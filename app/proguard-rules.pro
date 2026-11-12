# Preserve WebView bridge names in minified release. No production signing credentials are configured.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
