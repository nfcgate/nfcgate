extern "C" {
#include <xhook.h>
#include <xh_core.h>
}

#include <nfcd/hook/impl/XHook.h>

XHook::XHook(const std::string &name, void *hookFn, void *libraryHandle, const std::string &reLibrary) :
        IHook(name, hookFn, libraryHandle),
        mReLibrary(reLibrary) {
    hook();
}

void XHook::hook() {
    int r = xhook_register(mReLibrary.c_str(), mName.c_str(), mHookFn, &mAddress);
    LOG_ASSERT_X(r == 0, "xhook failed: %d", r);
}
