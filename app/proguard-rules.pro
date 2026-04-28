# Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.nidcard.app.data.entity.** { *; }
-keep class com.nidcard.app.data.entity.NIDCard

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
