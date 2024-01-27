#ifndef NFCD_SYMBOLTABLE_H
#define NFCD_SYMBOLTABLE_H

#include <string>
#include <unordered_map>

#include <nfcd/error.h>

class SymbolTable {
public:
    bool create(const std::string &library) {
        LOG_ASSERT_S(parse(library), return false, "Symbol table missing from library");
        return true;
    }

    bool contains(const std::string &name) const {
        return mSymbols.find(getName(name)) != mSymbols.end();
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
        return it == mSymbolsAlternativeName.end() ? name : it->second;
    }

protected:
    bool parse(const std::string &library);

    // symbol name -> symbol size
    std::unordered_map<std::string, unsigned long> mSymbols;
    // demangled symbol name -> (mangled) symbol name
    std::unordered_map<std::string, std::string> mSymbolsAlternativeName;
};

#endif //NFCD_SYMBOLTABLE_H
