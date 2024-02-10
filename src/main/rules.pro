-dontobfuscate
-keepattributes SourceFile, LineNumberTable

-allowaccessmodification

-keep class io.github.goooler.exporter.MainKt {
  public static void main(java.lang.String[]);
}

-dontwarn aQute.bnd.**
-dontwarn org.osgi.**

# Used by poi.
-keep,allowoptimization,allowshrinking class org.apache.logging.log4j.** { *; }
