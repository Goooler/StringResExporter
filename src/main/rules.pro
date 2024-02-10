-dontobfuscate
-keepattributes SourceFile, LineNumberTable

-allowaccessmodification

-keep class io.github.goooler.exporter.MainKt {
  public static void main(java.lang.String[]);
}

# Suppress warnings in R8 internal.
-dontwarn com.android.tools.r8.internal.**

-dontwarn aQute.bnd.**
-dontwarn org.osgi.**

# Used by poi.
-keep,allowoptimization,allowshrinking class org.apache.logging.log4j.** { *; }
