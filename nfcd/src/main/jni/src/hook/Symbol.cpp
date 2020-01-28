#include <dlfcn.h>
#include <unistd.h>
#include <sys/mman.h>

#include <nfcd/hook/Symbol.h>
#include <nfcd/helper/SymbolTable.h>

extern "C" {
#include <sys/system_properties.h>
}

Symbol::Symbol(const std::string &name, void *libraryHandle) {
    mName = SymbolTable::instance()->getName(name);
    mAddress = dlsym(libraryHandle, mName.c_str());
    LOG_ASSERT_X(mAddress, "Missing symbol: %s", name.c_str());
}
