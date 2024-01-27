#include <nfcd/helper/MapInfo.h>
#include <nfcd/nfcd.h>

#include <fstream>
#include <regex>

bool MapInfo::create() {
    std::ifstream maps("/proc/self/maps");
    LOG_ASSERT_S(maps.is_open(), return false, "Error loading proc maps");

    // line has format "<rangeStart>-<rangeEnd> <perms> <ignored1> <ignored2> ...    <label>"
    std::regex re(R"(^([^\s-]+)-([^\s]+)\s([\w-]+)\s.*?(?:\s\s([^\s].+))?$)");
    for (std::string line; std::getline(maps, line); ) {
        // match regex, ignore non-matching lines
        std::smatch matches;
        if (!std::regex_match(line, matches, re))
            continue;

        RangeData result;
        // parse start and end from hex
        result.start = std::stoull(matches.str(1), nullptr, 16);
        result.end = std::stoull(matches.str(2), nullptr, 16);
        // copy label
        result.label = matches.str(4);

        // perms has format "rwxp" or "----"
        auto perm = matches.str(3);
        LOG_ASSERT_S(perm.size() == 4, return false, "Error reading map perms");
        int read = perm[0] == 'r' ? 4 : 0;
        int write = perm[1] == 'w' ? 2 : 0;
        int execute = perm[2] == 'x' ? 1 : 0;
        result.perms = read + write + execute;

        mRanges.push_back(result);
    }

    return true;
}

std::set<std::string> MapInfo::loadedLibraries() const {
    std::set<std::string> result;

    for (auto &range : mRanges) {
        if (StringUtil::strEndsWith(range.label, ".so")) {
            result.emplace(range.label);
        }
    }

    return result;
}

void *MapInfo::getBaseAddress(const std::string &library) const {
    for (auto &range : mRanges) {
        // skip range that does not match the library
        if (!StringUtil::strEndsWith(range.label, library))
            continue;

        // skip range without read permission or without enough space for ELF header
        if ((range.perms & 4) != 4 || range.end - range.start <= 4)
            continue;

        // check ELF magic bytes to confirm this region as the base
        if (memcmp((void *)range.start, "\x7f" "ELF", 4) != 0)
            continue;

        return (void *)range.start;
    }

    return nullptr;
}

const MapInfo::RangeData *MapInfo::rangeFromAddress(uintptr_t addr, uint64_t size) const {
    for (auto &range : mRanges)
        if (addr >= range.start && (addr + size) <= range.end)
            return &range;

    return nullptr;
}
