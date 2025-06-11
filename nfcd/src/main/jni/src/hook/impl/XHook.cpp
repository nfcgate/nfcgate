extern "C" {
#include <xhook.h>
#include <xh_core.h>
}

#include <nfcd/hook/impl/XHook.h>

XHook::XHook(void *libraryHandle, const SymbolTable &symbolTable, const std::string &name, void *hookFn,
             const std::string &reLibrary) : Hook(libraryHandle, symbolTable, name, hookFn), mReLibrary(reLibrary) {
    int r = xhook_register(mReLibrary.c_str(), mDemangledName.c_str(), hookFn, &mAddress);
    LOG_ASSERT_S(r == 0, return, "XHook failed: %d", r);

    // looks like success, but xHook will not install the hook until refresh is called
    mHooked = true;
}
