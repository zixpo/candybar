-optimizationpasses 5
-overloadaggressively
-dontpreverify
-repackageclasses 'o'
-allowaccessmodification

-keep class **.R
-keep class **.R$* {
    <fields>;
}

# Keep the source line when using ProGuard
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile