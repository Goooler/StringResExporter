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
-dontwarn de.rototor.pdfbox.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn org.apache.jcp.xml.dsig.**
-dontwarn org.apache.pdfbox.pdmodel.**
-dontwarn org.apache.xml.security.**
-dontwarn org.bouncycastle.**
-dontwarn org.brotli.dec.**
-dontwarn org.objectweb.asm.**
-dontwarn org.osgi.**
-dontwarn org.tukaani.xz.**
-dontwarn org.w3c.dom.svg.**
-dontwarn edu.umd.cs.findbugs.annotations.**

# Used by poi.
-keep,allowoptimization class org.apache.logging.log4j.** { *; }
-keep,allowoptimization class org.apache.commons.compress.** { *; }
-keep,allowoptimization class org.apache.poi.** { *; }
