#include <jni.h>
#include <string>
#include "libyuv.h"

//分别用来存储1420，1420缩放，I420旋转和镜像的数据
static jbyte *Src_i420_data = nullptr;
static jbyte *Src_i420_data_scale = nullptr;
static jbyte *Src_i420_data_rotate = nullptr;

void freeAll();

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved) {
    freeAll();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_util_libyuv_YUVUtil_update(JNIEnv *env, jclass thiz, jint width, jint height,
                                            jint dst_width, jint dst_height) {
    if (nullptr != Src_i420_data) {
        free(Src_i420_data);
    }
    Src_i420_data = (jbyte *) malloc(sizeof(jbyte) * width * height * 3 / 2);
    if (nullptr != Src_i420_data_scale) {
        free(Src_i420_data_scale);
    }
    Src_i420_data_scale = (jbyte *) malloc(sizeof(jbyte) * dst_width * dst_height * 3 / 2);
    if (nullptr != Src_i420_data_rotate) {
        free(Src_i420_data_rotate);
    }
    Src_i420_data_rotate = (jbyte *) malloc(sizeof(jbyte) * dst_width * dst_height * 3 / 2);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_util_libyuv_YUVUtil_free(JNIEnv *env, jclass thiz) {
    freeAll();
}

void freeAll() {
    if (nullptr != Src_i420_data) {
        free(Src_i420_data);
        Src_i420_data = nullptr;
    }
    if (nullptr != Src_i420_data_scale) {
        free(Src_i420_data_scale);
        Src_i420_data_scale = nullptr;
    }
    if (nullptr != Src_i420_data_rotate) {
        free(Src_i420_data_rotate);
        Src_i420_data_rotate = nullptr;
    }
}

void scaleI420(jbyte *src_i420_data, jint width, jint height, jbyte *dst_i420_data, jint dst_width,
               jint dst_height, jint mode) {

    jint src_i420_y_size = width * height;
    jint src_i420_u_size = (width >> 1) * (height >> 1);
    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    jint dst_i420_y_size = dst_width * dst_height;
    jint dst_i420_u_size = (dst_width >> 1) * (dst_height >> 1);
    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + dst_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + dst_i420_y_size + dst_i420_u_size;

    libyuv::I420Scale((const uint8_t *) src_i420_y_data, width,
                      (const uint8_t *) src_i420_u_data, width >> 1,
                      (const uint8_t *) src_i420_v_data, width >> 1,
                      width, height,
                      (uint8_t *) dst_i420_y_data, dst_width,
                      (uint8_t *) dst_i420_u_data, dst_width >> 1,
                      (uint8_t *) dst_i420_v_data, dst_width >> 1,
                      dst_width, dst_height,
                      (libyuv::FilterMode) mode);
}

void rotateI420(jbyte *src_i420_data, jint width, jint height, jbyte *dst_i420_data, jint degree) {
    jint src_i420_y_size = width * height;
    jint src_i420_u_size = (width >> 1) * (height >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + src_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + src_i420_y_size + src_i420_u_size;

    //要注意这里的width和height在旋转之后是相反的
    if (degree == libyuv::kRotate90 || degree == libyuv::kRotate270) {
        libyuv::I420Rotate((const uint8_t *) src_i420_y_data, width,
                           (const uint8_t *) src_i420_u_data, width >> 1,
                           (const uint8_t *) src_i420_v_data, width >> 1,
                           (uint8_t *) dst_i420_y_data, height,
                           (uint8_t *) dst_i420_u_data, height >> 1,
                           (uint8_t *) dst_i420_v_data, height >> 1,
                           width, height,
                           (libyuv::RotationMode) degree);
    }
}

void mirrorI420(jbyte *src_i420_data, jint width, jint height, jbyte *dst_i420_data) {
    jint src_i420_y_size = width * height;
    jint src_i420_u_size = (width >> 1) * (height >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + src_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + src_i420_y_size + src_i420_u_size;

    libyuv::I420Mirror((const uint8_t *) src_i420_y_data, width,
                       (const uint8_t *) src_i420_u_data, width >> 1,
                       (const uint8_t *) src_i420_v_data, width >> 1,
                       (uint8_t *) dst_i420_y_data, width,
                       (uint8_t *) dst_i420_u_data, width >> 1,
                       (uint8_t *) dst_i420_v_data, width >> 1,
                       width, height);
}


void i420ToARGB(jbyte *argb_array, jbyte *src_i420_data, jint width, jint height) {
    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;

    libyuv::I420ToARGB(
            (const uint8_t *) src_i420_y_data, width,
            (const uint8_t *) src_i420_u_data, width >> 1,
            (const uint8_t *) src_i420_v_data, width >> 1,
            (uint8_t *) argb_array, width * 4,
            width, height);
}


void i420ToABGR(jbyte *argb_array, jbyte *src_i420_data, jint width, jint height) {
    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;

    libyuv::I420ToABGR(
            (const uint8_t *) src_i420_y_data, width,
            (const uint8_t *) src_i420_u_data, width >> 1,
            (const uint8_t *) src_i420_v_data, width >> 1,
            (uint8_t *) argb_array, width * 4,
            width, height);
}


void i420ToRGB(jbyte *argb_array, jbyte *src_i420_data, jint width, jint height) {
    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;

    libyuv::I420ToRGB24(
            (const uint8_t *) src_i420_y_data, width,
            (const uint8_t *) src_i420_u_data, width >> 1,
            (const uint8_t *) src_i420_v_data, width >> 1,
            (uint8_t *) argb_array, width * 3,
            width, height);
}


void i420ToRGBA(jbyte *rgba_array, jbyte *src_i420_data, jint width, jint height) {
    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;

    libyuv::I420ToRGBA(
            (const uint8_t *) src_i420_y_data, width,
            (const uint8_t *) src_i420_u_data, width >> 1,
            (const uint8_t *) src_i420_v_data, width >> 1,
            (uint8_t *) rgba_array, width * 4,
            width, height);
}


void nv21ToARGB(jbyte *argb_array, jbyte *src_nv21_data, jint width, jint height) {
    jint src_y_size = width * height;

    jbyte *src_nv21_y_data = src_nv21_data;
    jbyte *src_nv21_vu_data = src_nv21_data + src_y_size;

    libyuv::NV21ToARGB(
            (const uint8_t *) src_nv21_y_data, width,
            (const uint8_t *) src_nv21_vu_data, width,
            (uint8_t *) argb_array, width * 4,
            width, height);
}


void argbToNV21(jbyte *src_nv21_data, jbyte *argb_array, jint width, jint height) {
    jint src_y_size = width * height;

    jbyte *src_nv21_y_data = src_nv21_data;
    jbyte *src_nv21_vu_data = src_nv21_data + src_y_size;

    libyuv::ARGBToNV21(
            (const uint8_t *) argb_array, width * 4,
            (uint8_t *) src_nv21_y_data, width,
            (uint8_t *) src_nv21_vu_data, width,
            width, height);
}


void nv21ToI420(jbyte *src_nv21_data, jint width, jint height, jbyte *src_i420_data) {
    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *src_nv21_y_data = src_nv21_data;
    jbyte *src_nv21_vu_data = src_nv21_data + src_y_size;

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;

    libyuv::NV21ToI420((const uint8_t *) src_nv21_y_data, width,
                       (const uint8_t *) src_nv21_vu_data, width,
                       (uint8_t *) src_i420_y_data, width,
                       (uint8_t *) src_i420_u_data, width >> 1,
                       (uint8_t *) src_i420_v_data, width >> 1,
                       width, height);
}


void i420ToNV21(jbyte *src_i420_data, jint width, jint height, jbyte *src_nv21_data) {
    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *src_nv21_y_data = src_nv21_data;
    jbyte *src_nv21_vu_data = src_nv21_data + src_y_size;

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;

    libyuv::I420ToNV21((const uint8_t *) src_i420_y_data, width,
                       (const uint8_t *) src_i420_u_data, width >> 1,
                       (const uint8_t *) src_i420_v_data, width >> 1,
                       (uint8_t *) src_nv21_y_data, width,
                       (uint8_t *) src_nv21_vu_data, width,
                       width, height);
}

void ABGRToI420(jbyte *argb_array, jbyte *dst_i420_data, jint width, jint height) {
    jint dst_y_size = width * height;
    jint dst_u_size = (width >> 1) * (height >> 1);

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + dst_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + dst_y_size + dst_u_size;

    libyuv::ABGRToI420(
            (const uint8_t *) argb_array, width << 2,
            (uint8_t *) dst_i420_y_data, width,
            (uint8_t *) dst_i420_u_data, width >> 1,
            (uint8_t *) dst_i420_v_data, width >> 1,
            width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_util_libyuv_YUVUtil_compressYUV(JNIEnv *env, jclass thiz, jbyteArray src_,
                                                 jint width, jint height, jbyteArray dst_,
                                                 jint dst_width, jint dst_height, jint mode,
                                                 jint degree, jboolean is_mirror) {
    jbyte *Src_data = env->GetByteArrayElements(src_, NULL);
    jbyte *Dst_data = env->GetByteArrayElements(dst_, NULL);
    //nv21转化为i420
    nv21ToI420(Src_data, width, height, Src_i420_data);
    //进行缩放的操作
    scaleI420(Src_i420_data, width, height, Src_i420_data_scale, dst_width, dst_height, mode);
    if (is_mirror) {
        //进行旋转的操作
        rotateI420(Src_i420_data_scale, dst_width, dst_height, Src_i420_data_rotate, degree);
        //因为旋转的角度都是90和270，那后面的数据width和height是相反的
        mirrorI420(Src_i420_data_rotate, dst_height, dst_width, Dst_data);
    } else {
        rotateI420(Src_i420_data_scale, dst_width, dst_height, Dst_data, degree);
    }
    env->ReleaseByteArrayElements(dst_, Dst_data, 0);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_android_util_libyuv_YUVUtil_copyYUV(JNIEnv *env, jclass thiz, jbyteArray src_, jint width,
                                             jint height, jbyteArray dst_, jint dst_width,
                                             jint dst_height, jint left, jint top) {
    //裁剪的区域大小不对
    if (left + dst_width > width || top + dst_height > height) {
        return false;
    }

    //left和top必须为偶数，否则显示会有问题
    if (left % 2 != 0 || top % 2 != 0) {
        return false;
    }

    jint src_length = env->GetArrayLength(src_);
    jbyte *src_i420_data = env->GetByteArrayElements(src_, NULL);
    jbyte *dst_i420_data = env->GetByteArrayElements(dst_, NULL);


    jint dst_i420_y_size = dst_width * dst_height;
    jint dst_i420_u_size = (dst_width >> 1) * (dst_height >> 1);

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + dst_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + dst_i420_y_size + dst_i420_u_size;

    libyuv::ConvertToI420((const uint8_t *) src_i420_data, src_length,
                          (uint8_t *) dst_i420_y_data, dst_width,
                          (uint8_t *) dst_i420_u_data, dst_width >> 1,
                          (uint8_t *) dst_i420_v_data, dst_width >> 1,
                          left, top,
                          width, height,
                          dst_width, dst_height,
                          libyuv::kRotate0, libyuv::FOURCC_I420);

    env->ReleaseByteArrayElements(dst_, dst_i420_data, 0);
    return true;
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_android_util_libyuv_YUVUtil_rotateNV21(
        JNIEnv *env, jclass clazz,
        jbyteArray dst_nv21,
        jbyteArray src_nv21,
        jint width, jint height,
        jint rotate) {

    jbyte *dstNV21 = (*env).GetByteArrayElements(dst_nv21, 0);
    jbyte *srcNV21 = (*env).GetByteArrayElements(src_nv21, 0);

    //nv21转化为i420
    nv21ToI420(srcNV21, width, height, Src_i420_data);
    //进行旋转的操作
    rotateI420(Src_i420_data, width, height, Src_i420_data_rotate, rotate);
    //因为旋转的角度都是90和270，那后面的数据width和height是相反的
    i420ToNV21(Src_i420_data_rotate, height, width, dstNV21);

    (*env).ReleaseByteArrayElements(src_nv21, srcNV21, 0);
    (*env).ReleaseByteArrayElements(dst_nv21, dstNV21, 0);
    return true;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_android_util_libyuv_YUVUtil_transformNV21ToI420(
        JNIEnv *env, jclass clazz,
        jbyteArray dst_i420,
        jbyteArray src_nv21,
        jint width, jint height,
        jint rotate) {

    jbyte *srcNV21 = (*env).GetByteArrayElements(src_nv21, 0);
    jbyte *dstI420 = (*env).GetByteArrayElements(dst_i420, 0);

    //nv21转化为i420
    nv21ToI420(srcNV21, width, height, Src_i420_data);
    //进行旋转的操作
    rotateI420(Src_i420_data, width, height, dstI420, rotate);

    (*env).ReleaseByteArrayElements(src_nv21, srcNV21, 0);
    (*env).ReleaseByteArrayElements(dst_i420, dstI420, 0);
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_android_util_libyuv_YUVUtil_transformNV21ToRGBA(JNIEnv *env, jclass clazz,
                                                         jbyteArray dst_rgba,
                                                         jbyteArray src_nv21,
                                                         jint width, jint height,
                                                         jint rotate) {

    if (nullptr == Src_i420_data || nullptr == Src_i420_data_rotate) {
        return false;
    }

    jbyte *srcNV21 = (*env).GetByteArrayElements(src_nv21, 0);
    jbyte *dstRGBA = (*env).GetByteArrayElements(dst_rgba, 0);

    //nv21转化为i420
    nv21ToI420(srcNV21, width, height, Src_i420_data);
    //进行旋转的操作
    rotateI420(Src_i420_data, width, height, Src_i420_data_rotate, rotate);
    //因为旋转的角度都是90和270，那后面的数据width和height是相反的
    i420ToABGR(dstRGBA, Src_i420_data_rotate, height, width);

    (*env).ReleaseByteArrayElements(src_nv21, srcNV21, 0);
    (*env).ReleaseByteArrayElements(dst_rgba, dstRGBA, 0);

    return true;
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_android_util_libyuv_YUVUtil_compressNV21ToRGBA(JNIEnv *env, jclass clazz,
                                                        jbyteArray dst_rgba,
                                                        jbyteArray src_nv21,
                                                        jint width, jint height,
                                                        jint dst_width, jint dst_height,
                                                        jint rotate) {

    if (nullptr == Src_i420_data || nullptr == Src_i420_data_scale || nullptr == Src_i420_data_rotate) {
        return false;
    }

    jbyte *srcNV21 = (*env).GetByteArrayElements(src_nv21, 0);
    jbyte *dstRGBA = (*env).GetByteArrayElements(dst_rgba, 0);

    //nv21转化为i420
    nv21ToI420(srcNV21, width, height, Src_i420_data);
    //进行缩放的操作
    scaleI420(Src_i420_data, width, height, Src_i420_data_scale, dst_width, dst_height, 3);
    //进行旋转的操作
    rotateI420(Src_i420_data_scale, dst_width, dst_height, Src_i420_data_rotate, rotate);
    //因为旋转的角度都是90和270，那后面的数据width和height是相反的
    i420ToABGR(dstRGBA, Src_i420_data_rotate, dst_height, dst_width);

    (*env).ReleaseByteArrayElements(src_nv21, srcNV21, 0);
    (*env).ReleaseByteArrayElements(dst_rgba, dstRGBA, 0);

    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_android_util_libyuv_YUVUtil_transformRGBAToI420(JNIEnv *env, jclass clazz,
                                                         jbyteArray dst_i420, jbyteArray src_rgba,
                                                         jint width, jint height) {
    if (dst_i420 == nullptr || src_rgba == nullptr) return false;
    jbyte *srcRGBA = (*env).GetByteArrayElements(src_rgba, 0);
    jbyte *dstI420 = (*env).GetByteArrayElements(dst_i420, 0);
    ABGRToI420(srcRGBA, dstI420, width, height);
    (*env).ReleaseByteArrayElements(dst_i420, dstI420, 0);
    (*env).ReleaseByteArrayElements(src_rgba, srcRGBA, 0);
    return true;
}