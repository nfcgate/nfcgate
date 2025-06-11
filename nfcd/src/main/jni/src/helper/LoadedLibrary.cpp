#include "nfcd/helper/LoadedLibrary.h"

#include "nfcd/nfcd.h"

bool LoadedLibrary::createSymbolTable() {
    return mSymbolTable.create(mName);
}

bool LoadedLibrary::findLibraryHandle() {
    mHandle = globals.getLibraryHandle(mName.c_str());
    return mHandle != nullptr;
}

Symbol_ref LoadedLibrary::findSymbol(const std::string &name) const {
    auto result = std::make_shared<LibrarySymbol>(mHandle, mSymbolTable, name);
    LOG_ASSERT_S(result->isValid(), return {}, "Symbol %s not found in library %s", name.c_str(), mName.c_str());

    return result;
}

Hook_ref LoadedLibrary::findAndHook(const std::string &name, void *hookFn) const {
    Hook_ref result = Hook::hook(mHandle, mSymbolTable, name, hookFn, mNameRegex);
    LOG_ASSERT_S(result->isHooked(), return {}, "Hooking %s in library %s failed", name.c_str(), mName.c_str());
    return result;
}

bool LoadedLibrary::findAndHookOnce(Hook_ref &result, const std::string &name, void *hookFn) const {
    if (!result || !result->isHooked())
        result = findAndHook(name, hookFn);

    return result && result->isHooked();
}
