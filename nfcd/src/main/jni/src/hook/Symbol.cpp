#include <dlfcn.h>
#include <unistd.h>

#include <nfcd/nfcd.h>

Symbol::Symbol(const std::string &name) {
    mName = name;
    mAddress = dlsym(RTLD_DEFAULT, mName.c_str());
    LOG_ASSERT_S(mAddress, return, "Missing default symbol: %s", name.c_str());
}

Symbol::Symbol(const std::string &name, void *libraryHandle) {
    mName = globals.symbolTable.getName(name);
    mAddress = dlsym(libraryHandle, mName.c_str());
    LOG_ASSERT_S(mAddress, return, "Missing library symbol: %s", name.c_str());
}
