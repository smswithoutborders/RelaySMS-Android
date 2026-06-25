# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Suppress logging
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int e(...);
    public static int i(...);
    public static int v(...);
    public static int w(...);
}

#-dontwarn androidx.window.extensions.embedding.ActivityStack$Token
#-dontwarn androidx.window.extensions.embedding.SplitInfo$Token
-dontwarn com.squareup.okhttp.internal.Network

-dontwarn java.awt.Component
-dontwarn java.awt.GraphicsEnvironment
-dontwarn java.awt.HeadlessException
-dontwarn java.awt.Window

-keep class com.sun.jna.** { *; }
-keepclassmembers class * implements com.sun.jna.* { *; }
-dontwarn com.sun.jna.**

# Keep UniFFI internal structures and fields
-keep class uniffi.** { *; }
-keepclassmembers class uniffi.** { *; }

# Prevent JNA from losing field reflection
-keepclassmembers class * extends com.sun.jna.Structure {
    *** capacity;
    *** data;
    *** len;
}

