extern "C" {
#include <xhook.h>
}

#include <nfcd/nfcd.h>
#include <nfcd/hook/IHook.h>
#include <nfcd/hook/impl/XHook.h>
#include <nfcd/hook/impl/ADBIHook.h>
#include <nfcd/helper/System.h>

/* static */ bool IHook::useXHook = false;

void IHook::init() {
    // Pie = 28
    IHook::useXHook = System::sdkInt() >= System::P;
}

bool IHook::hookOnce(std::shared_ptr<IHook> &result, const std::string &name, void *hook) {
    if (!result || !result->isHooked()) {
        auto temp = IHook::hook(name, hook, globals.mHandle, globals.mLibraryRe);
        LOG_ASSERT_S(temp->isHooked(), return false, "Hooking failed for %s", name.c_str());
        result = temp;
    }

    return true;
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
