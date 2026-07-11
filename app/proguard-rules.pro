# MovieNest Log — R8 / ProGuard rules
# Keep kotlinx.serialization generated serializers for our data models.

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep the serialization runtime.
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes and their synthetic serializer companions.
-keep,includedescriptorclasses class com.movienest.log.**$$serializer { *; }
-keepclassmembers class com.movienest.log.data.model.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.movienest.log.data.model.** { *; }

# Keep enum values used through serialization / reflection.
-keepclassmembers enum com.movienest.log.data.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Compose already ships consumer rules; nothing extra required here.
