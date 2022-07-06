#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <stdio.h>
#include <math.h>
#include<queue>
#include <time.h>

#define LOG_TAG "SmoothBlur"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define GET_R_FROM_B_ENDIAN_RGBA8888(inVal) ((inVal & 0xff000000)>>24)
#define GET_G_FROM_B_ENDIAN_RGBA8888(inVal) ((inVal & 0x00ff0000)>>16)
#define GET_B_FROM_B_ENDIAN_RGBA8888(inVal) ((inVal & 0x0000ff00)>>8)
#define GET_A_FROM_B_ENDIAN_RGBA8888(inVal) (inVal & 0x000000ff)
#define GET_RGBA888_FROM_R_G_B_A(r, g, b, a) (((r << 24) & 0xff000000) | ((g << 16) & 0x00ff0000) | ((b << 8) & 0x0000ff00) | (a & 0x000000ff))

using namespace std;

void smoothBlend3(AndroidBitmapInfo *info, void *blurPixels, void *oriPixels, jint x, jint y,
                  jint inRadius, jint outRadius) {
    int xx = 0;
    int yy = 0;
    float powX2 = 0;
    float powY2 = 0;
    float powZ2 = 0;
    float scale = 0;
    int32_t temp = 0;
    int32_t curXStart = 0;
    int32_t curXEnd = 0;
    int32_t top = max(y - outRadius, 0);
    int32_t bottom = min(y + outRadius, (int) info->height);
    int32_t left = max(x - outRadius, 0);
    int32_t right = min(x + outRadius, (int) info->width);;
    uint32_t *blurLine = NULL;
    uint32_t *oriLine = NULL;
    uint32_t blurPixel = 0;
    uint32_t oriPixel = 0;
    uint32_t alpha = 0;
    uint32_t red = 0;
    uint32_t green = 0;
    uint32_t blue = 0;

    blurPixels = (char *) blurPixels + info->stride * top;
    oriPixels = (char *) oriPixels + info->stride * top;

    for (yy = top; yy < bottom; yy++) {
        powY2 = powf(yy - y, 2);
        powZ2 = powf(outRadius, 2);
        temp = (int32_t) sqrtf(powZ2 - powY2);
        curXStart = max(x - temp, left);
        curXEnd = min(x + temp, right);

        blurLine = (uint32_t *) blurPixels;
        oriLine = (uint32_t *) oriPixels;

        for (xx = curXStart; xx < curXEnd; xx++) {
            powX2 = powf(xx - x, 2);
            powY2 = powf(yy - y, 2);
            temp = (int) sqrtf(powX2 + powY2);
            if (temp < inRadius) {
                blurLine[xx] = oriLine[xx];
            } else if (temp < outRadius) {
                scale = (temp - inRadius) / (float) (outRadius - inRadius);
                blurPixel = blurLine[xx];
                oriPixel = oriLine[xx];
                alpha = (uint32_t) (GET_A_FROM_B_ENDIAN_RGBA8888(blurPixel) * scale) +
                                 (uint32_t) (GET_A_FROM_B_ENDIAN_RGBA8888(oriPixel) * (1 - scale));
                red = (uint32_t) (GET_R_FROM_B_ENDIAN_RGBA8888(blurPixel) * scale) +
                               (uint32_t) (GET_R_FROM_B_ENDIAN_RGBA8888(oriPixel) * (1 - scale));
                green = (uint32_t) (GET_G_FROM_B_ENDIAN_RGBA8888(blurPixel) * scale) +
                                 (uint32_t) (GET_G_FROM_B_ENDIAN_RGBA8888(oriPixel) * (1 - scale));
                blue = (uint32_t) (GET_B_FROM_B_ENDIAN_RGBA8888(blurPixel) * scale) +
                                (uint32_t) (GET_B_FROM_B_ENDIAN_RGBA8888(oriPixel) * (1 - scale));
                blurLine[xx] = GET_RGBA888_FROM_R_G_B_A(red, green, blue, alpha);
            }
        }
        blurPixels = (char *) blurPixels + info->stride;
        oriPixels = (char *) oriPixels + info->stride;
    }
}

void smoothBlend(AndroidBitmapInfo *info, void *blurPixels, void *oriPixles, jint x, jint y,
                 jint inRadius, jint outRadius) {
    int top = y - outRadius;
    int bottom = y + outRadius;
    int left = x - outRadius;
    int right = x + outRadius;
    if (top < 0) {
        top = 0;
    }
    if (bottom > info->height) {
        bottom = info->height;
    }
    if (left < 0) {
        left = 0;
    }
    if (right > info->width) {
        right = info->width;
    }
    int yy;
    blurPixels = (char *) blurPixels + info->stride * top;
    oriPixles = (char *) oriPixles + info->stride * top;
    for (yy = top; yy < bottom; yy++) {
        uint32_t *blurLine = (uint32_t *) blurPixels;
        uint32_t *oriLine = (uint32_t *) oriPixles;
        int xx;
        for (xx = left; xx < right; xx++) {
            float powX2 = powf(xx - x, 2);
            float powY2 = powf(yy - y, 2);
            int r = (int) sqrtf(powX2 + powY2);
            if (r < inRadius) {
                blurLine[xx] = oriLine[xx];
            } else if (r < outRadius) {

                float scale = (r - inRadius) / (float) (outRadius - inRadius);
                uint32_t blurPixel = blurLine[xx];
                uint32_t oriPixel = oriLine[xx];
                uint32_t alpha = (uint32_t) (GET_A_FROM_B_ENDIAN_RGBA8888(blurPixel) * scale) +
                                 (uint32_t) (GET_A_FROM_B_ENDIAN_RGBA8888(oriPixel) * (1 - scale));
                uint32_t red = (uint32_t) (GET_R_FROM_B_ENDIAN_RGBA8888(blurPixel) * scale) +
                               (uint32_t) (GET_R_FROM_B_ENDIAN_RGBA8888(oriPixel) * (1 - scale));
                uint32_t green = (uint32_t) (GET_G_FROM_B_ENDIAN_RGBA8888(blurPixel) * scale) +
                                 (uint32_t) (GET_G_FROM_B_ENDIAN_RGBA8888(oriPixel) * (1 - scale));
                uint32_t blue = (uint32_t) (GET_B_FROM_B_ENDIAN_RGBA8888(blurPixel) * scale) +
                                (uint32_t) (GET_B_FROM_B_ENDIAN_RGBA8888(oriPixel) * (1 - scale));
                blurLine[xx] = GET_RGBA888_FROM_R_G_B_A(red, green, blue, alpha);
            }
        }
        blurPixels = (char *) blurPixels + info->stride;
        oriPixles = (char *) oriPixles + info->stride;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_slrblur_SmoothBlurJni_smoothRender(
        JNIEnv *env,
        jclass clazz,
        jobject blur_bitmap,
        jobject ori_bitmap,
        jobject info) {

    AndroidBitmapInfo blurInfo;
    AndroidBitmapInfo oriInfo;
    void *blurPixels;
    void *oriPixels;
    int ret;

    jclass objClass = (*env).GetObjectClass(info);
    if (objClass == NULL) {
        LOGE("get object class failed");
    }

    jfieldID xId = (*env).GetFieldID(objClass, "x", "I");
    jfieldID yId = (*env).GetFieldID(objClass, "y", "I");
    jfieldID inRadiusId = (*env).GetFieldID(objClass, "inRadius", "I");
    jfieldID outRadiusId = (*env).GetFieldID(objClass, "outRadius", "I");

    jint x = (*env).GetIntField(info, xId);
    jint y = (*env).GetIntField(info, yId);
    jint inRadius = (*env).GetIntField(info, inRadiusId);
    jint outRadius = (*env).GetIntField(info, outRadiusId);

    LOGE("x = %d,y = %d,radius = %d", x, y, inRadius, outRadius);

    if ((ret = AndroidBitmap_getInfo(env, blur_bitmap, &blurInfo)) < 0) {
        LOGE("blurBitmap getInfo failed! error = %d", ret);
        return;
    }

    if ((ret = AndroidBitmap_getInfo(env, ori_bitmap, &oriInfo)) < 0) {
        LOGE("oriBitmap getInfo failed! error = %d", ret);
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, blur_bitmap, &blurPixels)) < 0) {
        LOGE("blurBitmap lockPixels failed ! error = %d", ret);
    }

    if ((ret = AndroidBitmap_lockPixels(env, ori_bitmap, &oriPixels)) < 0) {
        LOGE("oriBitmap lockPixels failed ! error = %d", ret);
    }
    LOGE("handle smooth blur info format:%d", blurInfo.format);
    smoothBlend3(&blurInfo, blurPixels, oriPixels, x, y, inRadius, outRadius);
    AndroidBitmap_unlockPixels(env, blur_bitmap);
    AndroidBitmap_unlockPixels(env, ori_bitmap);
}