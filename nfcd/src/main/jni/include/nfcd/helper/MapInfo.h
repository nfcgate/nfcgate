#ifndef NFCD_MAPINFO_H
#define NFCD_MAPINFO_H

#include <set>
#include <string>
#include <vector>

class MapInfo {
public:
    struct RangeData {
        uint64_t start, end;
        uint8_t perms;
        std::string label;
    };

    bool create();

    std::set<std::string> loadedLibraries() const;
    void *getBaseAddress(const std::string &library) const;
    const RangeData *rangeFromAddress(uintptr_t addr, uint64_t size = 0) const;

protected:
    std::vector<RangeData> mRanges;
};

#endif //NFCD_MAPINFO_H
