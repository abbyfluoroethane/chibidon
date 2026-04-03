# Keep Gson model classes
-keep class org.chibidon.model.** { *; }
-keep class org.chibidon.api.SavedAccount { *; }
-keep class org.chibidon.SyncedCredentials { *; }

# Gson TypeToken (required for R8)
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
