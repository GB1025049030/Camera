#继承Drawable....不进行混淆
-keep public class * extends android.graphics.drawable.Drawable

# Gson相关（实体类...不能进行混淆）
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod
-keep class com.android.library.baidu.aip.data.**{*;}

-keep class com.just.agentweb.**{*;}
