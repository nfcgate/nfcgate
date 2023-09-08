#ifndef NFCD_SYMBOL_H
#define NFCD_SYMBOL_H

#include <cstdint>
#include <functional>
#include <type_traits>
#include <string>

#include <nfcd/error.h>

class Symbol {
public:
    explicit Symbol(const std::string &name, void *libraryHandle);

    template <typename Fn, typename... Args>
    typename std::result_of<Fn*(Args...)>::type call(Args&&... args) {
        return ((Fn*)mAddress)(std::forward<Args>(args)...);
    }

    template<typename T>
    T *address() const {
        return reinterpret_cast<T*>(mAddress);
    }

protected:
    void *mAddress = nullptr;
    std::string mName;
};

using Symbol_ref = std::shared_ptr<Symbol>;

#endif //NFCD_SYMBOL_H
