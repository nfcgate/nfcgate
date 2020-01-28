#ifndef NFCD_IHOOK_H
#define NFCD_IHOOK_H

#include <nfcd/hook/Symbol.h>

class IHook : public Symbol {
public:
    bool isHooked() const {
        return mHooked;
    }

    virtual void precall() {};

    virtual void postcall() {};

    static void init();
    static IHook *hook(const std::string &name, void *hook, void *libraryHandle,
                       const std::string &reLibrary);
    static void finish();

protected:
    IHook(const std::string &name, void *hook, void *libraryHandle);

    static bool useXHook;

    virtual void hook() = 0;

    void *mHookFn = nullptr;
    bool mHooked = false;
};

#endif //NFCD_IHOOK_H
