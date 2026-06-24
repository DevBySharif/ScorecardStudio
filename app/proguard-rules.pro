# Keep JavaScriptInterface methods for WebView bridge
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep app classes used by WebView/reflection
-keep class com.example.scorecardstudio.WebAppInterface { *; }
-keep class com.example.scorecardstudio.RootDetector { *; }
-keep class com.example.scorecardstudio.FirebaseHelper { *; }
-keep class com.footballeon.scorecardstudio.** { *; }

# Obfuscate all other app classes aggressively
-repackageclasses ''
-allowaccessmodification
-flattenpackagehierarchy ''

# Firebase / Crashlytics
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# OkHttp/Okio (used by Firebase)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }

# Strips all logs, printStackTrace, and debug info in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
    public static java.lang.String getStackTraceString(java.lang.Throwable);
}

-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
    public java.lang.String toString();
}

-assumenosideeffects class java.lang.Exception {
    public void printStackTrace();
}

# Remove debug metadata
-assumenosideeffects class android.os.Build {
    public static *** *;
}

# Compact the output
-optimizationpasses 5
-overloadaggressively
-useuniqueclassmembernames
