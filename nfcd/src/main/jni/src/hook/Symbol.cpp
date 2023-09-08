#include <dlfcn.h>
#include <unistd.h>

#include <nfcd/nfcd.h>

Symbol::Symbol(const std::string &name, void *libraryHandle) {
    mName = globals.symbolTable.getName(name);
    mAddress = dlsym(libraryHandle, mName.c_str());
    LOG_ASSERT_S(mAddress, return, "Missing symbol: %s", name.c_str());
}
