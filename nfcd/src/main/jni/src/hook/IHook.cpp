extern "C" {
#include <xhook.h>
}

#include <nfcd/hook/IHook.h>
#include <nfcd/hook/impl/XHook.h>
#include <nfcd/hook/impl/ADBIHook.h>
#include <nfcd/helper/System.h>

/* static */ bool IHook::useXHook = false;

void IHook::init() {
    // Pie = 28
    IHook::useXHook = System::sdkInt() >= System::P;
}

IHook_ref IHook::hook(const std::string &name, void *hook, void *libraryHandle,
                   const std::string &reLibrary) {
    if (useXHook)
        return IHook_ref(new XHook(name, hook, libraryHandle, reLibrary));
    else
        return IHook_ref(new ADBIHook(name, hook, libraryHandle));
}

bool IHook::finish() {
    if (useXHook)
        return xhook_refresh(0) == 0;

    return true;
}

IHook::IHook(const std::string &name, void *hookFn, void *libraryHandle) :
        Symbol(name, libraryHandle), mHookFn(hookFn) {}
