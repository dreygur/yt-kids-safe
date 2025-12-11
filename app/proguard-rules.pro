# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.ytkidssafe.**$$serializer { *; }
-keepclassmembers class com.ytkidssafe.** {
    *** Companion;
}
-keepclasseswithmembers class com.ytkidssafe.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Rhino JavaScript Engine (used by NewPipe extractor)
-keep class org.mozilla.** { *; }
-keepclassmembers class org.mozilla.** { *; }
-dontwarn org.mozilla.**

# NewPipe Extractor
-keep class org.schabi.newpipe.extractor.** { *; }
-keepclassmembers class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**

# Missing Java SE classes not available on Android
-dontwarn java.beans.**
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn javax.script.**
-dontwarn javax.naming.**
-dontwarn sun.misc.**

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Jsoup
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# Keep all annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# Ignore all missing class warnings for R8
-ignorewarnings
