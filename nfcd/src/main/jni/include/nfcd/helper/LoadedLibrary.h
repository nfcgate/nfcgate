#ifndef NFCGATE_LOADEDLIBRARY_H
#define NFCGATE_LOADEDLIBRARY_H

#include "nfcd/helper/StringUtil.h"
#include "nfcd/helper/SymbolTable.h"
#include "nfcd/hook/Symbol.h"
#include "nfcd/hook/Hook.h"

class SymbolTable;

class LoadedLibrary {
public:
    LoadedLibrary() = default;
    explicit LoadedLibrary(const std::string &name) :
            mName(name), mNameRegex("^" + StringUtil::escapeBRE(mName) + "$") {

    }

    bool empty() const {
        return mName.empty() || !mHandle;
    }

    const std::string &name() const {
        return mName;
    }
    const std::string &regex() const {
        return mNameRegex;
    }
    const SymbolTable &symbolTable() const {
        return mSymbolTable;
    }
    void *handle() const {
        return mHandle;
    }

    bool createSymbolTable();
    bool findLibraryHandle();

    Symbol_ref findSymbol(const std::string &name) const;
    Hook_ref findAndHook(const std::string &name, void *hookFn) const;
    bool findAndHookOnce(Hook_ref &result, const std::string &name, void *hookFn) const;

protected:
    std::string mName, mNameRegex;
    SymbolTable mSymbolTable;
    void *mHandle = nullptr;
};

#endif //NFCGATE_LOADEDLIBRARY_H
