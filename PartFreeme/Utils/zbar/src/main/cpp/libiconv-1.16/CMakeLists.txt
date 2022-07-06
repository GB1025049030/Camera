cmake_minimum_required(VERSION 3.4.1)
add_compile_options(
        -Wno-multichar
        -DANDROID
        -DLIBDIR=\"c\"
        -DBUILDING_LIBICONV
        -DIN_LIBRARY
)
include_directories(include libcharset libcharset/include)
#aux_source_directory(lib libcharset SRC_FILE)
add_library(iconv SHARED lib/iconv.c lib/relocatable.c libcharset/lib/localcharset.c)
find_library(log-lib log)
target_link_libraries(iconv ${log-lib})