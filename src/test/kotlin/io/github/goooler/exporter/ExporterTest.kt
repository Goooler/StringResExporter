package io.github.goooler.exporter

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ExporterTest {

  @Test
  fun res2xls(@TempDir tempDir: Path) {
    val inputPath = tempDir.resolve("res")
    inputPath.outputStream().buffered().use {
      tempDir::class.java.getResourceAsStream("/res")!!.copyTo(it)
    }
    val outputPath = tempDir.absolutePathString()

    println("inputPath  " + inputPath.absolutePathString())
    println("outputPath  " + outputPath)

    CommandLineTestRunner(tempDir, "--res2xls", inputPath.absolutePathString(), outputPath)

    assert(Paths.get(outputPath).resolve("output.xls").exists())
  }
}
