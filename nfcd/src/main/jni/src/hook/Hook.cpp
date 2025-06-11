extern "C" {
#include <xhook.h>
}

#include <nfcd/nfcd.h>
#include <nfcd/hook/Hook.h>
#include <nfcd/hook/impl/XHook.h>
#include <nfcd/hook/impl/ADBIHook.h>
#include <nfcd/helper/System.h>

/* static */ bool Hook::useADBI = false;

void Hook::init() {
    // Pie = 28
    useADBI = System::sdkInt() < System::P;

    // enable XHook debug logging to logcat as "xhook" tag
    xhook_enable_debug(1);
}

Hook_ref Hook::hook(void *libraryHandle, const SymbolTable &symbolTable,
                    const std::string &name, void *hookFn, const std::string &reLibrary) {
    if (useADBI)
        return std::make_shared<ADBIHook>(libraryHandle, symbolTable, name, hookFn);
    else
        return std::make_shared<XHook>(libraryHandle, symbolTable, name, hookFn, reLibrary);
}

bool Hook::finish() {
    if (!useADBI)
        return xhook_refresh(0) == 0;

    return true;
}

Hook::Hook(void *libraryHandle, const SymbolTable &symbolTable, const std::string &name, void *hookFn)
        : LibrarySymbol(libraryHandle, symbolTable, name), mHookFn(hookFn) {

}
