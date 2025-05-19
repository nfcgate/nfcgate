#ifndef NFCD_SYMBOL_H
#define NFCD_SYMBOL_H

#include <cstdint>
#include <functional>
#include <type_traits>
#include <string>

#include <nfcd/error.h>

class Symbol {
public:
    static std::shared_ptr<Symbol> findDefault(const std::string &name);
    static std::shared_ptr<Symbol> findInLibrary(const std::string &name);

    template <typename Fn, typename... Args>
    typename std::result_of<Fn*(Args...)>::type call(Args&&... args) {
        return ((Fn*)mAddress)(std::forward<Args>(args)...);
    }

    template<typename T>
    T *address() const {
        return reinterpret_cast<T*>(mAddress);
    }

protected:
    Symbol(const std::string &name, void *address) : mName(name), mAddress(address) { }

    std::string mName;
    void *mAddress = nullptr;
};

using Symbol_ref = std::shared_ptr<Symbol>;

#endif //NFCD_SYMBOL_H
