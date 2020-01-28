#ifndef NFCD_SYMBOLTABLE_H
#define NFCD_SYMBOLTABLE_H

#include <link.h>
#include <sys/mman.h>

#include <string>
#include <unordered_map>
#include <cxxabi.h>

#include <nfcd/error.h>

class SymbolTable {
public:
    static void create(const char *file) {
        mInstance = new SymbolTable(file);
    }

    static SymbolTable *instance() {
        return mInstance;
    }

    unsigned long getSize(const std::string &name) const {
        auto it = mSymbols.find(getName(name));

        if (it == mSymbols.end()) {
            LOGE("Symbol %s missing from SymbolTable", name.c_str());
            return 0;
        }

        return it->second;
    }

    std::string getName(const std::string &name) const {
        auto it = mSymbolsAlternativeName.find(name);

        if (it == mSymbolsAlternativeName.end()) {
            LOGE("Alternative symbol name for %s missing from SymbolTable", name.c_str());
            return name;
        }

        return it->second;
    }

protected:
    SymbolTable(const char *file) {
        FILE *phy = fopen(file, "rb");
        fseek(phy, 0, SEEK_END);
        long phy_size = ftell(phy);

        mBase = mmap(nullptr, (size_t)phy_size, PROT_READ, MAP_PRIVATE, fileno(phy), 0);
        fclose(phy);
        LOG_ASSERT_X(mBase != MAP_FAILED, "Loading library via mmap failed with %d", errno);

        LOG_ASSERT(parse(), "Symbol table missing from library");
        munmap(mBase, (size_t)phy_size);
    }

    std::string demangle(const std::string &name) const {
        std::string result = name;

        char *res = abi::__cxa_demangle(name.c_str(), nullptr, nullptr, nullptr);
        if (res != nullptr) {
            std::string demangled(res);

            auto pos = demangled.find("(");
            if (pos != std::string::npos)
                result = demangled.substr(0, pos);
        }
        free(res);

        return result;
    }

    bool parse() {
        char *base = (char*)mBase;
        ElfW(Ehdr) *header = (ElfW(Ehdr) *) base;

        // section info
        size_t tbl_string_off = 0, tbl_string_sz = 0,
                tbl_symbol_off = 0, tbl_symbol_sz = 0;

        // get section header names
        ElfW(Shdr) *shstr = (ElfW(Shdr) *) (base + header->e_shoff + header ->e_shstrndx * header->e_shentsize);
        size_t tbl_shnames_off = shstr->sh_offset;

        // iterate program headers
        for (uint16_t i = 0; i < header->e_shnum; i++) {
            ElfW(Shdr) *section = (ElfW(Shdr) *) (base + header->e_shoff + i * header->e_shentsize);

            std::string sname(base + tbl_shnames_off + section->sh_name);
            switch (section->sh_type) {
                case SHT_STRTAB:
                    // need only the string table of dynamic strings
                    if (sname == ".dynstr") {
                        tbl_string_off = section->sh_offset;
                        tbl_string_sz = section->sh_size;
                    }
                    break;
                case SHT_DYNSYM:
                    tbl_symbol_off = section->sh_offset;
                    tbl_symbol_sz = section->sh_size;
                    break;

                default:
                    break;
            }
        }

        // not found -> should never happen
        if (tbl_string_off == 0 || tbl_string_sz == 0 || tbl_symbol_off == 0
            || tbl_symbol_sz == 0)
            return false;

        ElfW(Sym) *symbol = (ElfW(Sym) *)(base + tbl_symbol_off);
        ElfW(Sym) *end = (ElfW(Sym) *)(base + tbl_symbol_off + tbl_symbol_sz);

        for (; symbol != end; symbol++) {
            if (symbol->st_name != 0) {
                std::string name(base + tbl_string_off + symbol->st_name);
                mSymbols.emplace(name, symbol->st_size);
                mSymbolsAlternativeName.emplace(demangle(name), name);
            }
        }

        return true;
    }

    void *mBase;
    std::unordered_map<std::string, unsigned long> mSymbols;
    // demangled -> mangled
    std::unordered_map<std::string, std::string> mSymbolsAlternativeName;

    static SymbolTable *mInstance;
};

#endif //NFCD_SYMBOLTABLE_H
