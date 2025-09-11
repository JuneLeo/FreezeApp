cmake_minimum_required(VERSION 3.4.1)

project("freezeapp")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

#set(C_FLAGS "-Werror=format -fdata-sections -ffunction-sections -fno-exceptions -fno-rtti -fno-threadsafe-statics")
#set(LINKER_FLAGS "-Wl,--hash-style=both")
#
#if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
#    message("Builing Release...")
#
#    set(C_FLAGS "${C_FLAGS} -O2 -fvisibility=hidden -fvisibility-inlines-hidden")
#    set(LINKER_FLAGS "${LINKER_FLAGS} -Wl,-exclude-libs,ALL -Wl,--gc-sections")
#else()
#    message("Builing Debug...")
#
#    add_definitions(-DDEBUG)
#endif ()


#
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${C_FLAGS}")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${C_FLAGS}")

#set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} ${LINKER_FLAGS}")
#set(CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS} ${LINKER_FLAGS}")


find_library(log-lib log)
add_library(freeze SHARED freeze.cpp)
target_link_libraries(freeze ${log-lib})
