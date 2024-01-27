#include <nfcd/helper/SymbolTable.h>

#include "ELFMemoryParser.h"

bool SymbolTable::parse(const std::string &library) {
    auto addSymbol = [&] (const std::string &name, const std::string &demangled, size_t size) {
        mSymbols.emplace(name, size);
        mSymbolsAlternativeName.emplace(demangled, name);
    };

    return ELFMemoryParser::parse(library, addSymbol);
}