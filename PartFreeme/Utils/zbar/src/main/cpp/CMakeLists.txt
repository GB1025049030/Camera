cmake_minimum_required(VERSION 3.4.1)
include_directories(zbar/android/jni/)
include_directories(zbar/include)
include_directories(zbar/zbar)
include_directories(libcharset/include)
include_directories(libiconv-1.16/include)
add_subdirectory(libiconv-1.16)
add_library(zbarjni SHARED
        zbar/java/zbarjni.c
        zbar/zbar/img_scanner.c
        zbar/zbar/decoder.c
        zbar/zbar/image.c
        zbar/zbar/symbol.c
        zbar/zbar/convert.c
        zbar/zbar/config.c
        zbar/zbar/scanner.c
        zbar/zbar/error.c
        zbar/zbar/refcnt.c
        zbar/zbar/video.c
        zbar/zbar/video/null.c
        zbar/zbar/decoder/code128.c
        zbar/zbar/decoder/code39.c
        zbar/zbar/decoder/code93.c
        zbar/zbar/decoder/codabar.c
        zbar/zbar/decoder/databar.c
        zbar/zbar/decoder/ean.c
        zbar/zbar/decoder/i25.c
        zbar/zbar/decoder/qr_finder.c
        zbar/zbar/qrcode/bch15_5.c
        zbar/zbar/qrcode/binarize.c
        zbar/zbar/qrcode/isaac.c
        zbar/zbar/qrcode/qrdec.c
        zbar/zbar/qrcode/qrdectxt.c
        zbar/zbar/qrcode/rs.c
        zbar/zbar/qrcode/util.c
        )
find_library(log-lib log)
target_link_libraries(zbarjni ${log-lib} iconv)