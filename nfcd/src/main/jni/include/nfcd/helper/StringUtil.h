#ifndef NFCD_STRINGUTIL_H
#define NFCD_STRINGUTIL_H

#include <string>
#include <sstream>

class StringUtil {
public:
    static std::string toLower(const std::string &s) {
        std::string result;
        std::transform(s.begin(), s.end(), std::back_inserter(result), ::tolower);

        return result;
    }

    static bool strContains(const std::string &s, const std::string &q) {
        return s.find(q) != std::string::npos;
    }
    static bool strContainsAny(const std::string &s, const std::initializer_list<std::string> &qs) {
        for (auto &q : qs)
            if (strContains(s, q))
                return true;

        return false;
    }
    static bool strStartsWith(const std::string &s, const std::string &q) {
        return q.size() <= s.size() && std::equal(q.begin(), q.end(), s.begin());
    }
    static bool strEndsWith(const std::string &s, const std::string &q) {
        return q.size() <= s.size() && std::equal(q.rbegin(), q.rend(), s.rbegin());
    }

    static std::string escapeBRE(const std::string &in) {
        std::stringstream bruce;

        for (char c : in) {
            switch (c) {
                case '.':
                case '[':
                case ']':
                case '^':
                case '$':
                case '*':
                case '\\':
                    bruce << "\\" << c;
                    break;

                default:
                    bruce << c;
                    break;
            }
        }

        return bruce.str();
    }
};

#endif //NFCD_STRINGUTIL_H
