# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

#set(CMAKE_CXX_FLAGS "-std=c++11 -g -fPIE")
#set(CMAKE_SHARED_LINKER_FLAGS "-fPIE -pie")

# fix a bug with newer NDKs
# https://stackoverflow.com/questions/44793617/android-ndk-error-asm-types-h-not-found
if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
else()
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
endif()

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
             nfcgate-native

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/jni/main.cpp
             src/main/jni/chip.cpp
             src/main/jni/javainterface.cpp
             src/main/jni/vendor/adbi/hook.cpp
             )

target_link_libraries(nfcgate-native android log)
