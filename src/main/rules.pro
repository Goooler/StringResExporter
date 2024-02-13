-allowaccessmodification
-dontobfuscate
-keepattributes SourceFile, LineNumberTable

-keep class io.github.goooler.exporter.MainKt {
  public static void main(java.lang.String[]);
}

-dontwarn aQute.bnd.**
-dontwarn org.osgi.**
-dontwarn edu.umd.cs.findbugs.annotations.**

# TODO: remove these after the Clikt bundled Mordant is updated to 2.3.0
-dontwarn org.graalvm.nativeimage.**
-dontwarn com.oracle.svm.core.annotate.Delete
-dontwarn org.graalvm.word.PointerBase

# Used by poi.
-keep,allowoptimization class org.apache.logging.log4j.** { *; }
