package io.github.goooler.exporter

import java.nio.charset.StandardCharsets
import java.nio.file.Path

class CommandLineTestRunner(
  private val tempDir: Path,
  private val converter: String,
  private val inputPath: String,
  private val outputPath: String,
) : Runnable {

  override fun run() {
    val process = ProcessBuilder(cliCommand(converter, inputPath, outputPath)).apply {
      directory(tempDir.toFile())
    }.start()
    process.waitFor()
    val err = process.errorStream.readBytes().toString(StandardCharsets.UTF_8)
//    val out = process.inputStream.readBytes().toString(StandardCharsets.UTF_8)
    if (err.isNotEmpty()) {
      error("Error occurred when running command line: $err")
    }
  }

  companion object {
    private val cliPath = System.getProperty("CLI_PATH") ?: error("CLI_PATH must not be null.")

    private fun cliCommand(vararg arguments: String): String = buildList {
      add("java")
      add("-jar")
      add(cliPath)
      addAll(arguments)
    }.joinToString(separator = " ")
  }
}
