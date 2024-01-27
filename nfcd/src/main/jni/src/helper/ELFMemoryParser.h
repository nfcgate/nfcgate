#include <link.h>
#include <dlfcn.h>

#include <cxxabi.h>

#include <nfcd/nfcd.h>

static std::string demangle(const std::string &name) {
    std::string result = name;

    char *res = abi::__cxa_demangle(name.c_str(), nullptr, nullptr, nullptr);
    if (res != nullptr) {
        std::string demangled(res);
        free(res);

        auto pos = demangled.find("(");
        if (pos != std::string::npos)
            result = demangled.substr(0, pos);
    }

    return result;
}

class ELFMemoryParser {
    struct DynamicHeader_t {
        bool empty() const { return vaddr == 0 || size == 0; }

        size_t vaddr = 0;
        size_t size = 0;
        size_t count = 0;
    };
    struct SymbolTable_t {
        bool empty() const { return vaddr == 0 || entrySize == 0; }

        size_t vaddr = 0;
        size_t entrySize = 0;
        size_t count = 0;
    };
    struct StringTable_t {
        bool empty() const { return vaddr == 0; }

        size_t vaddr = 0;
    };
public:
    /// callback with (symbol name, demangled symbol name, symbol size)
    using Symbol_f = std::function<void(const std::string &, const std::string &, size_t)>;

    /// parse library symbols in memory and invoke cb for every named symbol
    static bool parse(const std::string &library, const Symbol_f &cb) {
        ELFMemoryParser parser;

        // use library name to find mBase
        LOG_ASSERT_S(parser.findBase(library), return false, "Could not find library base");
        // use mBase to read mLowestLoad for relocations and the location of PT_DYNAMIC from header
        LOG_ASSERT_S(parser.parseHeader(), return false, "Could not parse ELF header");
        // read PT_DYNAMIC to get symbol and string table location and sizes
        LOG_ASSERT_S(parser.parsePTDynamic(), return false, "Could not parse PT_DYNAMIC");
        // read symbol and string table to invoke callback for every symbol name and size
        LOG_ASSERT_S(parser.parseSymbols(cb), return false, "Could not parse symbol table");

        return true;
    }

protected:
    explicit ELFMemoryParser() = default;

    template<typename T>
    T *relocate(size_t vaddr) {
        return reinterpret_cast<T*>(mBase + vaddr - mLowestLoad);
    }

    bool findBase(const std::string &library) {
        // try to detect library base address
        mBase = (uint8_t *)globals.mapInfo.getBaseAddress(library);
        LOG_ASSERT_S(mBase, return false, "Error determining library base");

        return true;
    }

    bool parseHeader() {
        auto *header = (ElfW(Ehdr) *) mBase;
        mLowestLoad = std::numeric_limits<size_t>::max();

        // scan program headers
        for (uint16_t i = 0; i < header->e_phnum; i++) {
            auto *phdr = (ElfW(Phdr) *)(mBase + header->e_phoff + i * header->e_phentsize);

            switch (phdr->p_type) {
                case PT_LOAD:
                    mLowestLoad = std::min(mLowestLoad, (size_t)phdr->p_vaddr);
                    break;

                case PT_DYNAMIC:
                    mDynamic.vaddr = phdr->p_vaddr;
                    mDynamic.size = phdr->p_memsz;
                    break;
            }
        }

        LOG_ASSERT_S(mLowestLoad != std::numeric_limits<size_t>::max(),
                     return false, "Lowest PT_LOAD could not be determined");
        LOG_ASSERT_S(!mDynamic.empty(), return false, "PT_DYNAMIC not found");
        mDynamic.count = mDynamic.size / sizeof(ElfW(Dyn));

        return true;
    }

    uint32_t symbolCountDTHash(size_t hash_vaddr) {
        // 2nd word is the number of entries in the hash
        return relocate<ElfW(Word)>(hash_vaddr)[1];
    }

    // adapted from https://stackoverflow.com/a/57099317
    uint32_t symbolCountDTGnuHash(size_t hash_vaddr) {
        // See https://flapenguin.me/2017/05/10/elf-lookup-dt-gnu-hash/ and
        // https://sourceware.org/ml/binutils/2006-10/msg00377.html
        typedef struct
        {
            uint32_t nbuckets;
            uint32_t symoffset;
            uint32_t bloom_size;
            uint32_t bloom_shift;
        } DTGnuHeader;

        auto *header = relocate<DTGnuHeader>(hash_vaddr);
        const uint8_t* bucketsAddress =
                (uint8_t *)header + sizeof(DTGnuHeader) + (sizeof(uint64_t) * header->bloom_size);

        // Locate the chain that handles the largest index bucket.
        uint32_t lastSymbol = 0;
        uint32_t* bucketAddress = (uint32_t*)bucketsAddress;
        for (uint32_t i = 0; i < header->nbuckets; ++i) {
            uint32_t bucket = *bucketAddress;
            if (lastSymbol < bucket)
                lastSymbol = bucket;

            bucketAddress++;
        }

        if (lastSymbol < header->symoffset)
            return header->symoffset;

        // Walk the bucket's chain to add the chain length to the total.
        const uint8_t* chainBaseAddress = bucketsAddress + (sizeof(uint32_t) * header->nbuckets);
        for (;;) {
            uint32_t* chainEntry = (uint32_t*)(chainBaseAddress +
                    (lastSymbol - header->symoffset) * sizeof(uint32_t));
            lastSymbol++;

            // If the low bit is set, this entry is the end of the chain.
            if (*chainEntry & 1)
                break;
        }

        return lastSymbol;
    }

    bool parsePTDynamic() {
        // scan PT_DYNAMIC header
        for (uint32_t i = 0; i < mDynamic.count; i++) {
            auto *dyn = relocate<ElfW(Dyn)>(mDynamic.vaddr + i * sizeof(ElfW(Dyn)));

            switch (dyn->d_tag) {
                case DT_SYMTAB:
                    mSymbols.vaddr = dyn->d_un.d_ptr;
                    break;
                case DT_SYMENT:
                    mSymbols.entrySize = dyn->d_un.d_val;
                    break;

                case DT_HASH:
                    mSymbols.count = symbolCountDTHash(dyn->d_un.d_ptr);
                    break;
                case DT_GNU_HASH:
                    mSymbols.count = symbolCountDTGnuHash(dyn->d_un.d_ptr);
                    break;

                case DT_STRTAB:
                    mStrings.vaddr = dyn->d_un.d_ptr;
                    break;

                default:
                    break;
            }
        }

        // not found -> should never happen
        LOG_ASSERT_S(!mSymbols.empty(), return false, "Dynamic Symbol Table could not be found");
        LOG_ASSERT_S(!mStrings.empty(), return false, "String Table could not be found");
        return true;
    }

    bool parseSymbols(const Symbol_f &cb) {
        for (uint32_t i = 0; i < mSymbols.count; i++) {
            auto *sym = relocate<ElfW(Sym)>(mSymbols.vaddr + i * mSymbols.entrySize);

            // skip unnamed and undefined symbols
            if (sym->st_name != 0 && sym->st_value != 0) {
                std::string name(relocate<const char>(mStrings.vaddr + sym->st_name));

                cb(name, demangle(name), sym->st_size);
            }
        }

        return true;
    }

    // current base address
    uint8_t *mBase;
    // lowest address of all PT_LOADs to relocate against
    size_t mLowestLoad = 0;

    // PT_DYNAMIC program header
    DynamicHeader_t mDynamic;
    // symbol table
    SymbolTable_t mSymbols;
    // string table
    StringTable_t mStrings;
};