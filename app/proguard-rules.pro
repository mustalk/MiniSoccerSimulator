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

# --- ProGuard Rules for AndroidX and Google Libraries ---

# Preserve all classes and members in the androidx namespace
-keep class androidx.** { *; }
# Do not warn about any missing classes in the androidx namespace
-dontwarn androidx.**

# Preserve all classes and members in the Material Components library
-keep class com.google.android.material.** { *; }
# Do not warn about any missing classes in the Material Components library
-dontwarn com.google.android.material.**

# Preserve all classes and members in the ConstraintLayout library
-keep class androidx.constraintlayout.** { *; }
-dontwarn androidx.constraintlayout.**

# --- ProGuard Rules for Networking and Data Serialization ---

# Gson: Preserve classes used by Gson for serialization/deserialization
-keep class com.google.gson.stream.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
# Do not warn about missing classes in the sun.misc package (used by Gson)
-dontwarn sun.misc.**
# Preserve fields annotated with @SerializedName
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit: Preserve Retrofit classes and methods annotated with HTTP annotations
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp: Preserve OkHttp classes
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

# --- ProGuard Rules for Dependency Injection ---

# Hilt: Preserve Hilt and javax.inject classes and members
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**

# --- ProGuard Rules for Coroutines ---

# Kotlin Coroutines: Preserve coroutines classes and members
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# --- ProGuard Rules for Testing Libraries ---

# Mockito: Preserve Mockito classes and members
-dontwarn org.mockito.**
-keep class org.mockito.** { *; }

# JUnit: Preserve JUnit classes and members
-dontwarn junit.**
-keep class junit.** { *; }

# Espresso: Preserve Espresso classes and members
-dontwarn androidx.test.espresso.**
-keep class androidx.test.espresso.** { *; }

# --- Additional Configuration ---

# Preserve application-specific custom rules here if needed
# For example:
# -keep class com.example.myapp.** { *; }
