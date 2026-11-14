cmake_minimum_required(VERSION 3.22.1)
project(zani_native LANGUAGES C CXX)

add_library(zani_native SHARED native_bridge.cpp native_util.c)
target_compile_features(zani_native PRIVATE cxx_std_17)
target_compile_options(zani_native PRIVATE -Wall -Wextra)
