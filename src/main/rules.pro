-allowaccessmodification
-dontobfuscate
-keepattributes SourceFile, LineNumberTable

-keep class io.github.goooler.exporter.MainKt {
  public static void main(java.lang.String[]);
}

-dontwarn aQute.bnd.**
-dontwarn org.osgi.**

# TODO: remove these after the Clikt bundled Mordant is updated to 2.3.0
-dontwarn org.graalvm.nativeimage.**
-dontwarn com.oracle.svm.core.annotate.Delete
-dontwarn org.graalvm.word.PointerBase

# I don't need any loggers, remove them as much as possible.
-assumenosideeffects class org.apache.logging.log4j.** { *; }
