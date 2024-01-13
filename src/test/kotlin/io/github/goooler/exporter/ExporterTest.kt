package io.github.goooler.exporter

import assertk.assertThat
import assertk.assertions.isTrue
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.exists
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ExporterTest {

  @Test
  fun res2xls(@TempDir tempDir: Path) {
    val inputPath = tempDir.resolve("res")
    val outputPath = tempDir.absolutePathString()

    val sourcePath = Paths.get(this::class.java.getResource("/res")!!.toURI())
    sourcePath.copyToRecursively(inputPath)

    println("inputPath:  ${inputPath.absolutePathString()}")
    println("outputPath:  $outputPath")

    CommandLineTestRunner(tempDir, "--res2xls", inputPath.absolutePathString(), outputPath).run()

    assertThat(Paths.get(outputPath).resolve("output.xls").exists()).isTrue()
  }

  @OptIn(ExperimentalPathApi::class)
  private fun Path.copyToRecursively(target: Path) {
    copyToRecursively(target, followLinks = true, overwrite = true)
  }
}
