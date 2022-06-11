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

    template <typename Fn, typename... Args>
    typename std::result_of<Fn*(Args...)>::type callHook(Args&&... args) {
        return ((Fn*)mHookFn)(std::forward<Args>(args)...);
    }

    static void init();
    static IHook *hook(const std::string &name, void *hook, void *libraryHandle,
                       const std::string &reLibrary);
    static bool finish();

protected:
    IHook(const std::string &name, void *hook, void *libraryHandle);

    static bool useXHook;

    virtual void hookInternal() = 0;

    void *mHookFn = nullptr;
    bool mHooked = false;
};

#endif //NFCD_IHOOK_H
