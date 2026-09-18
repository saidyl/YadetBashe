# قوانین ProGuard/R8 برای YadetBashe

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Metadata برای Hilt و Room
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# ThreeTenABP
-keep class org.threeten.bp.** { *; }
