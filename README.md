### 源代码主要目录结构

DreamCamera2
├── android-ex-camera2-portability   展讯framework依赖（frameworks/ex/camera2/portability/）
├── android-ex-camera2-utils    展讯framework依赖（frameworks/ex/camera2/utils/）
├── app
│   └── src
│       └── main
│           └── java
│               └── com
│                   ├── dream
│                   │   └── camera
│                   │       ├── filter    ：滤镜模式自定义View和渲染器的实现
│                   │       ├── modules
│                   │       │   ├── AudioPicture    ：有声
│                   │       │   ├── **autophoto**  ：拍照
│                   │       │   ├── **autovideo**   ：视频
│                   │       │   ├── blurrefocus     ：原生景深
│                   │       │   ├── **continuephoto**   ：连拍
│                   │       │   ├── EffectVideo      ：美视
│                   │       │   ├── **filter**   ：滤镜
│                   │       │   ├── highresolutionphoto      ：48M
│                   │       │   ├── intentcapture     ：其他应用调用的拍照
│                   │       │   ├── intentvideo    ：其他应用调用的视频
│                   │       │   ├── intervalphoto    ：间隔
│                   │       │   ├── irphoto     ：红外检测
│                   │       │   ├── macrophoto    ：微距拍照
│                   │       │   ├── macrovideo      ：微距视频
│                   │       │   ├── manualphoto     ：专业
│                   │       │   ├── panoramadream     ：全景
│                   │       │   ├── portraitphoto     ：人像
│                   │       │   ├── **qr**     ：扫码模式
│                   │       │   ├── tdnrphoto    ：夜景拍照
│                   │       │   ├── tdnrvideo    ：夜景视频
│                   │       │   ├── tdphotomodule    ：3D相机
│                   │       │   ├── tdvideo       ：3D视频
│                   │       │   ├── timelapsevideo
│                   │       │   └── ultrawideangle     ：广角
│                   │       ├── settings   ：相机设置相关
│                   │       └── ui    ：相机主要的自定义View
│                   ├── freeme
│                   │   └── camera
│                   │        ├── common
│                   │        │   ├── custom    ：相机功能开关实现类
│                   │        │   ├── gradienter     ：水平仪
│                   │        │   └── location    ：百度定位
│                   │        └── modules
│                   │           ├── **ikophoto**    ：智能识物
│                   │           └── **slrphoto**   ：景深模式
│                   └── sprd
│                       └── camera
│                           ├── aidetection    ：AI控制类
│                           ├── encoder    ：音视频编解码相关
│                           ├── freeze    ：人脸属性控制类
│                           ├── panora   ：全景功能具体实现
│                           ├── storagepath   ：存储相关
│                           └── voice    ：音频录制
├── bd_aip   ：百度人脸识别功能管理模块
├── common    ：公共功能
├── help    ：T310平台兼容辅助模块
├── libbitmap     ：Bitmap管理模块，包括BitmapPool的封装
├── libslrblur    ：景深JNI实现
├── libyuv     ：libyuv库引入
└── xmp_toolkit     ：展讯XMPCore依赖（external/xmp_toolkit/XMPCore）

### Settings的默认值

以后摄拍照的设置中的部分功能来说明，如下：

<integer-array name="photo_back_auto_setting">
    ...
    <item>@array/pref_camera_zsl_key_array</item>
    ...
</integer-array>

<integer-array name="pref_camera_zsl_key_array">
	<!-- key -->
    <item>@string/pref_camera_zsl_key</item>
    <!-- storage position -->
    <item>@integer/storage_position_category_bf</item>
    <!-- default values -->
    <item>@string/preference_switch_item_default_value_true</item>
    <item>@array/preference_camera_switch_entryvalues</item>
    <item>@array/preference_camera_switch_entryvalues</item>
</integer-array>

如上述代码中所述，只需要修改对应mode中设置项对应的array中的默认值即可。

### Release版本调试打开所有log

adb shell setprop log.tag.CAM_ V
adb shell setprop log.tag.CAM_ D

#### 源代码由来

1. android-ex-camera2-portability（对应T7510的源码）

   源码路径：frameworks/ex/camera2/portability/

2. android-ex-camera2-utils（对应T7510的源码）

   源码路径：frameworks/ex/camera2/utils/

3. xmp_toolkit（对应T7510的源码）

   源码路径：external/xmp_toolkit/XMPCore

4. Source文件组成:

   default : src   src_pd    res    res_p    res_filter

   PRODUCT_USE_CAM_FILTER = false

   删除　src/com/dream/camera/filter/sprd
   删除　src/com/dream/camera/modules/filter/sprd
   添加　src_fake/com/dream/camera/modules/filter/sprd
   删除　sprdfilter.jar

