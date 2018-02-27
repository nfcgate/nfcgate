#ifndef NFCD_CONFIG
#define NFCD_CONFIG

#include <vector>

using config_ref = std::unique_ptr<uint8_t>;

class Option {
public:
    Option(uint8_t type, uint8_t *value, uint8_t len) : mType(type), mLen(len), mValue(value) {}

    uint8_t type() const {
        return mType;
    }

    uint8_t len() const {
        return mLen;
    }

    uint8_t *value() {
        return mValue;
    }

    void value(uint8_t *newValue, uint8_t newLen) {
        mValue = newValue;
        mLen = newLen;
    }

    void push(config_ref &config, uint8_t &offset) {
        /*
         * Each config option has:
         * - 1 byte type
         * - 1 byte length
         * - length byte data
         */
        config.get()[offset + 0] = mType;
        config.get()[offset + 1] = mLen;

        memcpy(&config.get()[offset + 2], mValue, mLen);
        offset += mLen + 2;
    }

protected:
    uint8_t mType, mLen;
    uint8_t *mValue;
};

class Config {
public:
    Config() = default;

    uint8_t total() const {
        return mTotal;
    }

    void add(uint8_t type, uint8_t *value, uint8_t len = 1) {
        mOptions.emplace_back(type, value, len);
    }

    void add(const Option &opt) {
        mOptions.push_back(opt);
    }

    void build(config_ref &config) {
        uint8_t offset = 0;

        // calculate total size of needed buffer
        for (auto &opt : mOptions)
            mTotal += opt.len() + 2;

        // allocate buffer
        config.reset(new uint8_t[mTotal]);

        // push each option to buffer
        for (auto &opt : mOptions)
            opt.push(config, offset);
    }

    void parse(uint8_t size, uint8_t *stream) {
        mOptions.clear();
        mTotal = 0;

        for (uint8_t offset = 0; offset < size - 2; ) {
            uint8_t type = stream[offset + 0];
            uint8_t len = stream[offset + 1];

            mOptions.emplace_back(type, &stream[offset + 2], len);
            offset += len + 2;
        }
    }

    std::vector<Option> &options() {
        return mOptions;
    }

protected:
    uint8_t mTotal = 0;

    std::vector<Option> mOptions;
};

#endif //NFCD_CONFIG
