# Keep the source line when using ProGuard
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# Needed for helpers.sharpie classes
# I guess they doesn't matter because all the methods we are using
# are part of the `android.**` package and `android` package remains consistent
# across devices
#-keepclassmembers class android.content.Context { <methods>; }
#-keepclassmembers class android.content.pm.PackageManager { <methods>; }
#-keepclassmembers class android.content.res.Resources { <methods>; }
#-keepclassmembers class android.widget.Toast { <methods>; }
#-keepclassmembers class androidx.appcompat.app.AppCompatActivity { <methods>; }

# LoganSquare JSON parser
-keep class com.bluelinelabs.logansquare.** { *; }
-keep @com.bluelinelabs.logansquare.annotation.JsonObject class *
-keep class **$$JsonObjectMapper { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.ConscryptHostnameVerifier

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl