#ifndef NFCD_XHOOK_H
#define NFCD_XHOOK_H

#include <nfcd/hook/IHook.h>

class XHook : public IHook {
public:
    XHook(const std::string &name, void *hook, void *libraryHandle, const std::string &reLibrary);

protected:
    void hook() override;

    std::string mReLibrary;
};

#endif //NFCD_XHOOK_H
