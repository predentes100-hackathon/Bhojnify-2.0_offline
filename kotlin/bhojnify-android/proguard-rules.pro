# R8 / ProGuard rules for Bhojnify Android

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature,Exceptions

-keepclassmembers class * {
    *** Companion;
}

-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    *** INSTANCE;
}

-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

-keepclassmembers class * extends kotlinx.serialization.internal.GeneratedSerializer {
    <init>(...);
}

-keepclassmembers class * {
    *** Companion;
    *** $$serializer;
}

# Bhojnify Core Models & State Serialization
-keep class com.bhojnify.core.model.** { *; }
-keepclassmembers class com.bhojnify.core.model.** {
    *** Companion;
    *** $$serializer;
    *;
}

-keep class com.bhojnify.core.state.** { *; }
-keepclassmembers class com.bhojnify.core.state.** { *; }

-keep class com.bhojnify.core.repository.** { *; }
-keep class com.bhojnify.core.service.** { *; }
-keep class com.bhojnify.core.i18n.** { *; }

# AndroidX ProfileInstaller
-keep class androidx.profileinstaller.** { *; }

# Coil
-keep class coil.** { *; }
