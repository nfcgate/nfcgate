cmake_minimum_required(VERSION 3.4.1)

# find files
file(GLOB_RECURSE NFCD_HEADERS ${CMAKE_CURRENT_SOURCE_DIR}/src/*.h)
file(GLOB_RECURSE NFCD_SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp)

# compile library
add_library(nfcgate SHARED ${NFCD_HEADERS} ${NFCD_SOURCES})

# link library
target_link_libraries(nfcgate android log)
