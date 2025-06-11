#ifndef NFCD_XHOOK_H
#define NFCD_XHOOK_H

#include <nfcd/hook/Hook.h>

class XHook : public Hook {
public:
    XHook(void *libraryHandle, const SymbolTable &symbolTable, const std::string &name,
          void *hookFn, const std::string &reLibrary);

protected:
    std::string mReLibrary;
};

#endif //NFCD_XHOOK_H
