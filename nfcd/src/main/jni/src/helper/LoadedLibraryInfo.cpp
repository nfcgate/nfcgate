#include "nfcd/helper/LoadedLibraryInfo.h"

#include "nfcd/nfcd.h"

bool LoadedLibraryInfo::createSymbolTable() {
    return mSymbolTable.create(mName);
}

bool LoadedLibraryInfo::findLibraryHandle() {
    mHandle = globals.getLibraryHandle(mName.c_str());
    return mHandle != nullptr;
}
