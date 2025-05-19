#include <dlfcn.h>
#include <unistd.h>

#include <nfcd/nfcd.h>

Symbol_ref Symbol::findDefault(const std::string &name) {
    // find default symbol address
    void *address = dlsym(RTLD_DEFAULT, name.c_str());
    LOG_ASSERT_S(address, return {}, "Missing default symbol: %s", name.c_str());

    return Symbol_ref(new Symbol(name, address));
}

Symbol_ref Symbol::findInLibrary(const std::string &name) {
    const auto demangledName = globals.symbolTable.getName(name);
    void *address = dlsym(globals.mHandle, demangledName.c_str());
    LOG_ASSERT_S(address, return {}, "Missing library symbol: %s", name.c_str());

    return Symbol_ref(new Symbol(name, address));
}
