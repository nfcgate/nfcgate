#ifndef NFCD_SYMBOL_H
#define NFCD_SYMBOL_H

#include <cstdint>
#include <functional>
#include <type_traits>
#include <string>

#include <nfcd/error.h>

class SymbolTable;

class Symbol {
public:
    virtual ~Symbol() = default;

    bool isValid() const {
        return mAddress != nullptr;
    }
    operator bool() const {
        return isValid();
    }

    const std::string &name() const {
        return mName;
    }
    template<typename T>
    T *address() const {
        return reinterpret_cast<T*>(mAddress);
    }

    template <typename Fn, typename... Args>
    typename std::result_of<Fn*(Args...)>::type call(Args&&... args) {
        return reinterpret_cast<Fn*>(mAddress)(std::forward<Args>(args)...);
    }

protected:
    Symbol(const std::string &name, void *address) : mName(name), mAddress(address) { }

    std::string mName;
    void *mAddress = nullptr;
};
using Symbol_ref = std::shared_ptr<Symbol>;

class DefaultSymbol : public Symbol {
public:
    explicit DefaultSymbol(const std::string &globalName);
};

class LibrarySymbol : public Symbol {
public:
    LibrarySymbol(void *libraryHandle, const SymbolTable &symbolTable, const std::string &name);

    const std::string &demangledName() const {
        return mDemangledName;
    }

protected:
    std::string mDemangledName;
};

#endif //NFCD_SYMBOL_H
