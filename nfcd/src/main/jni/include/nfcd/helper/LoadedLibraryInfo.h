#ifndef NFCGATE_LOADEDLIBRARYINFO_H
#define NFCGATE_LOADEDLIBRARYINFO_H

#include "nfcd/helper/StringUtil.h"
#include "nfcd/helper/SymbolTable.h"

class SymbolTable;

class LoadedLibraryInfo {
public:
    LoadedLibraryInfo() = default;
    explicit LoadedLibraryInfo(const std::string &name) :
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

protected:
    std::string mName, mNameRegex;
    SymbolTable mSymbolTable;
    void *mHandle = nullptr;
};

#endif //NFCGATE_LOADEDLIBRARYINFO_H
