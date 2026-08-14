cmake_minimum_required(VERSION 3.22.1)
project(aubio_android C CXX)

# 1. Grab all the core Aubio C files
file(GLOB_RECURSE AUBIO_SRC "aubio/src/*.c")

# 2. Create a static library for Aubio
add_library(aubio STATIC ${AUBIO_SRC})

# 3. Provide necessary compiler flags (replaces config.h)
target_compile_definitions(aubio PRIVATE
        -DHAVE_STDLIB_H
        -DHAVE_STDIO_H
        -DHAVE_MATH_H
        -DHAVE_STRING_H
        -DHAVE_LIMITS_H
        -DHAVE_C99_VARARGS_MACROS
)

# 4. Expose the headers so your JNI wrapper or mwengine can see them
target_include_directories(aubio PUBLIC "aubio/src")

# 5. Create your JNI wrapper (to bridge Kotlin and C++)
add_library(aubio_jni SHARED aubio_jni.cpp)

# Link Aubio, Android logging, and standard Android libs
target_link_libraries(aubio_jni
        aubio
        log
        android
)