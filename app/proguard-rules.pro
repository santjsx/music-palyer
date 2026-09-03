# ProGuard & R8 optimization rules for com.ipodmodern.audio
# -----------------------------------------------------------

# 1. Native JNI Methods & Audio Engine Bridge
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class com.ipodmodern.audio.core.audio.NativeAudioBridge { *; }
-keepclassmembers class com.ipodmodern.audio.core.audio.NativeAudioBridge {
    public static <methods>;
}

# Keep C++ native callback targets
-keep class com.ipodmodern.audio.core.audio.** { *; }

# 2. Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <fields>;
}

# 3. AndroidX Room Database & Entities
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep class com.ipodmodern.audio.core.database.entity.** { *; }
-keep interface com.ipodmodern.audio.core.database.dao.** { *; }

# 4. Data Models & Metadata
-keep class com.ipodmodern.audio.core.model.** { *; }

# 5. Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# 6. Embedded Ktor Server for Wi-Fi Sync
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn io.netty.**
-dontwarn org.slf4j.**

# 7. Media3 & ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
