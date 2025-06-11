#ifndef NFCD_HOOK_H
#define NFCD_HOOK_H

#include <nfcd/hook/Symbol.h>

class Hook : public LibrarySymbol {
    static bool useADBI;
public:
    static void init();
    static std::shared_ptr<Hook> hook(void *libraryHandle, const SymbolTable &symbolTable,
                                      const std::string &name, void *hookFn, const std::string &reLibrary);
    static bool finish();

    bool isHooked() const {
        return mHooked && LibrarySymbol::isValid();
    }

    virtual void preCall() {};
    virtual void postCall() {};

    template <typename Fn, typename... Args>
    typename std::result_of<Fn*(Args...)>::type callHook(Args&&... args) {
        return reinterpret_cast<Fn*>(mHookFn)(std::forward<Args>(args)...);
    }

protected:
    Hook(void *libraryHandle, const SymbolTable &symbolTable, const std::string &name, void *hookFn);

    void *mHookFn = nullptr;
    bool mHooked = false;
};
using Hook_ref = std::shared_ptr<Hook>;

#endif //NFCD_HOOK_H
