# Keep line numbers for readable crash reports.
-keepattributes SourceFile,LineNumberTable

# Firestore builds these objects by reflection, so their names and fields must survive.
-keep class com.kantu.pab_volunteers.data.model.** { *; }
-keepclassmembers class com.kantu.pab_volunteers.data.model.** {
    <init>();
    <fields>;
}

# Annotations Firestore reads at runtime to map documents.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Firebase and Play Services keep their own rules, these cover the reflective bits.
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
}

# Glide.
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }

# org.json is used to read the picture service reply.
-dontwarn org.json.**
