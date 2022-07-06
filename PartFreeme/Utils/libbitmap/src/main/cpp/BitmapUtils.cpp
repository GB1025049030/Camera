#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <cmath>

#define LOG_TAG "BitmapUtils"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define GET_A_FROM_B_ENDIAN_RGBA8888(inVal) ((inVal & 0xff000000u)>>24u)
#define GET_B_FROM_B_ENDIAN_RGBA8888(inVal) ((inVal & 0x00ff0000u)>>16u)
#define GET_G_FROM_B_ENDIAN_RGBA8888(inVal) ((inVal & 0x0000ff00u)>>8u)
#define GET_R_FROM_B_ENDIAN_RGBA8888(inVal) (inVal & 0x000000ffu)

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_android_camera_bitmap_BitmapManager_getAlphaBitmap(JNIEnv *env, jclass clazz,
                                                            jobject baseBitmap, jfloat baseAlpha) {
    // 透明度校验
    baseAlpha = baseAlpha > 1 ? 1 : baseAlpha < 0 ? 0 : baseAlpha;

    // 获取bitmap属性信息
    AndroidBitmapInfo baseInfo;
    AndroidBitmapInfo outputInfo;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_getInfo(env, baseBitmap, &baseInfo)) {
        LOGE("baseBitmap getInfo 参数异常");
        return false;
    }

    // 获取bitmap的像素信息,并锁住当前的像素点
    void *baseBuf;
    void *outputBuf;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, baseBitmap, &baseBuf)) {
        LOGE("baseBitmap lockPixels 参数异常");
        return false;
    }

    int w = baseInfo.width;
    int h = baseInfo.height;

    auto *basePixels = static_cast<uint32_t *>(baseBuf);
    uint32_t baseColor;
    uint32_t alpha;

    for (int i = 0; i < h; ++i) {
        for (int j = 0; j < w; ++j) {
            baseColor = basePixels[w * i + j];
            alpha = GET_A_FROM_B_ENDIAN_RGBA8888(baseColor) * baseAlpha;
            /*/ 灰度
            uint32_t alpha = ((color & 0xFF000000) >> 24);
            uint32_t red = ((color & 0x00FF0000) >> 16);
            uint32_t green = ((color & 0x0000FF00) >> 8);
            uint32_t blue = color & 0x000000FF;
            color = (red + green + blue) / 3;
            color = (alpha << 24) | (color << 16) | (color << 8) | color;
            //*/

            baseColor = (alpha << 24u) | (baseColor & 0x00FFFFFFu);
            basePixels[w * i + j] = baseColor;
        }
    }
    AndroidBitmap_unlockPixels(env, baseBitmap);
    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_android_camera_bitmap_BitmapManager_getMixAlphaBitmap(JNIEnv *env, jclass clazz,
                                                             jobject targetBitmap, jobject baseBitmap,
                                                             jfloat baseAlpha) {
    // 透明度校验
    baseAlpha = baseAlpha > 1 ? 1 : baseAlpha < 0 ? 0 : baseAlpha;

    // 获取bitmap属性信息
    AndroidBitmapInfo baseInfo;
    AndroidBitmapInfo targetInfo;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_getInfo(env, baseBitmap, &baseInfo)) {
        LOGE("baseBitmap getInfo 参数异常");
        return false;
    }
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_getInfo(env, targetBitmap, &targetInfo)) {
        LOGE("targetBitmap getInfo 参数异常");
        return false;
    }

    // 获取bitmap的像素信息,并锁住当前的像素点
    void *baseBuf;
    void *targetBuf;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, baseBitmap, &baseBuf)) {
        LOGE("baseBitmap lockPixels 参数异常");
        return false;
    }
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, targetBitmap, &targetBuf)) {
        LOGE("targetBitmap lockPixels 参数异常");
        return false;
    }

    int w = baseInfo.width;
    int h = baseInfo.height;
    if (w != targetInfo.width || h != targetInfo.height) {
        LOGE("Bitmap Size 异常");
        return false;
    }

    auto *basePixels = static_cast<uint32_t *>(baseBuf);
    auto *targetPixels = static_cast<uint32_t *>(targetBuf);
    uint32_t baseColor;
    uint32_t targetColor;
    uint32_t red;
    uint32_t green;
    uint32_t blue;

    for (int i = 0; i < h; ++i) {
        for (int j = 0; j < w; ++j) {
            targetColor = targetPixels[w * i + j];
            baseColor = basePixels[w * i + j];

//            red = (GET_R_FROM_B_ENDIAN_RGBA8888(targetColor) + GET_R_FROM_B_ENDIAN_RGBA8888(baseColor)) >> 2u;
//            green = (GET_G_FROM_B_ENDIAN_RGBA8888(targetColor) + GET_G_FROM_B_ENDIAN_RGBA8888(baseColor)) >> 2u;
//            blue = (GET_B_FROM_B_ENDIAN_RGBA8888(targetColor) + GET_B_FROM_B_ENDIAN_RGBA8888(baseColor)) >> 2u;

            red = (GET_R_FROM_B_ENDIAN_RGBA8888(targetColor) * (1 - baseAlpha) + GET_R_FROM_B_ENDIAN_RGBA8888(baseColor) * baseAlpha);
            green = (GET_G_FROM_B_ENDIAN_RGBA8888(targetColor) * (1 - baseAlpha) + GET_G_FROM_B_ENDIAN_RGBA8888(baseColor) * baseAlpha);
            blue = (GET_B_FROM_B_ENDIAN_RGBA8888(targetColor) * (1 - baseAlpha) + GET_B_FROM_B_ENDIAN_RGBA8888(baseColor) * baseAlpha);

            targetColor = 0xFF000000u | (blue << 16u) | (green << 8u) | red;
            targetPixels[w * i + j] = targetColor;
        }
    }
    AndroidBitmap_unlockPixels(env, baseBitmap);
    AndroidBitmap_unlockPixels(env, targetBitmap);
    return true;
}
