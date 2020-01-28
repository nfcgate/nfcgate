#include <sys/system_properties.h>
#include <string>

#include <nfcd/helper/System.h>

/* static */ int System::sSdkInt = -1;

int System::sdkInt() {
    if (sSdkInt != -1)
        return sSdkInt;

    char osVersion[PROP_VALUE_MAX+1];
    __system_property_get("ro.build.version.sdk", osVersion);
    int sdk_int = std::stoi(std::string(osVersion));

    sSdkInt = sdk_int;
    return sSdkInt;
}
