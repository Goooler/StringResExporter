-dontobfuscate
-keepattributes SourceFile, LineNumberTable

-allowaccessmodification

-keep class io.github.goooler.exporter.MainKt {
  public static void main(java.lang.String[]);
}

# Suppress warnings in R8 internal.
-dontwarn com.android.tools.r8.internal.**

-dontwarn aQute.bnd.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.apache.batik.**
-dontwarn org.objectweb.asm.**
-dontwarn org.tukaani.xz.**
-dontwarn org.brotli.dec.BrotliInputStream
-dontwarn net.sf.saxon.**
-dontwarn org.osgi.**
-dontwarn edu.umd.cs.findbugs.annotations.**

# Used by poi.
-keep,allowoptimization class org.apache.logging.log4j.** { *; }
-keep,allowoptimization class org.apache.commons.compress.** { *; }
