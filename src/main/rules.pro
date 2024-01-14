-dontobfuscate
-keepattributes SourceFile, LineNumberTable

-allowaccessmodification

-keep class io.github.goooler.exporter.MainKt {
  public static void main(java.lang.String[]);
}

# Suppress warnings in R8 internal.
-dontwarn com.android.tools.r8.internal.**

-dontwarn aQute.bnd.**
-dontwarn com.conversantmedia.util.concurrent.DisruptorBlockingQueue
-dontwarn com.conversantmedia.util.concurrent.SpinPolicy
-dontwarn com.fasterxml.jackson.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.lmax.disruptor.**
-dontwarn edu.umd.cs.findbugs.annotations.**
-dontwarn javax.activation.DataSource
-dontwarn javax.jms.**
-dontwarn javax.mail.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.kafka.**
-dontwarn org.codehaus.stax2.XMLStreamWriter2
-dontwarn org.fusesource.jansi.**
-dontwarn org.jctools.queues.**
-dontwarn org.osgi.**
-dontwarn org.zeromq.**

# Used by poi.
-keep,allowoptimization class org.apache.logging.log4j.** { *; }
-keep,allowoptimization class org.apache.logging.log4j.core.** { *; }
-keep,allowoptimization class org.apache.logging.log4j.simple.SimpleLogger { *; }
