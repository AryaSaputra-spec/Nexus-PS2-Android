
cmake_minimum_required(VERSION 3.22.1)

project(nexus_engine)

add_library(
    nexus_engine
    SHARED
    native-lib.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    nexus_engine
    ${log-lib}
)
