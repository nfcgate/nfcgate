#ifndef NFCD_IHOOK_H
#define NFCD_IHOOK_H

#include <nfcd/hook/Symbol.h>

class IHook : public Symbol {
    static bool useXHook;
public:
    static void init();
    static bool hookOnce(std::shared_ptr<IHook> &result, const std::string &name, void *hook);
    static std::shared_ptr<IHook> hook(const std::string &name, void *hook, void *libraryHandle,
                                       const std::string &reLibrary);
    static bool finish();
    bool isHooked() const {
        return mHooked;
    }

    virtual void precall() {};
    virtual void postcall() {};

    template <typename Fn, typename... Args>
    typename std::result_of<Fn*(Args...)>::type callHook(Args&&... args) {
        return ((Fn*)mHookFn)(std::forward<Args>(args)...);
    }

protected:
    IHook(const std::string &name, void *hook, void *libraryHandle)
            : Symbol(name, libraryHandle), mHookFn(hook) {

    }

    virtual void hookInternal() = 0;

    void *mHookFn = nullptr;
    bool mHooked = false;
};

using IHook_ref = std::shared_ptr<IHook>;

#endif //NFCD_IHOOK_H
