#include <dlfcn.h>
#include <unistd.h>

#include <nfcd/nfcd.h>

DefaultSymbol::DefaultSymbol(const std::string &globalName)
        : Symbol(globalName, nullptr) {

    // find default symbol address
    mAddress = dlsym(RTLD_DEFAULT, mName.c_str());
    LOG_ASSERT_S(mAddress, return, "Missing default symbol: %s", mName.c_str());

    LOGI("Default symbol %s found at %p", mName.c_str(), mAddress);
}

LibrarySymbol::LibrarySymbol(void *libraryHandle, const SymbolTable &symbolTable, const std::string &name)
        : Symbol(name, nullptr) {

    // find demangled name and address in the library
    mDemangledName = symbolTable.getName(name);
    mAddress = dlsym(libraryHandle, mDemangledName.c_str());
    LOG_ASSERT_S(mAddress, return, "Missing library symbol: %s", name.c_str());

    LOGI("Library symbol %s found at %p in library handle %p", mDemangledName.c_str(), mAddress, libraryHandle);
}
