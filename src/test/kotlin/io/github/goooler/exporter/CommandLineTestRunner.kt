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
    val process = ProcessBuilder(cliCommand(converter, inputPath, outputPath))
      .directory(tempDir.toFile())
      .start()
    val exitCode = process.waitFor()

    val err = process.errorStream.readBytes().toString(StandardCharsets.UTF_8)
    val out = process.inputStream.readBytes().toString(StandardCharsets.UTF_8)

    if (err.isNotEmpty() || exitCode != 0) {
      error("Error occurred when running command line: $err")
    }
    if (!out.startsWith(SUCCESS_OUTPUT)) {
      error("Output is not correct: $out")
    }
  }

  companion object {
    private val cliPath = System.getProperty("CLI_PATH") ?: error("CLI_PATH must not be null.")

    private fun cliCommand(vararg arguments: String) = buildList {
      add("java")
      add("-jar")
      add(cliPath)
      addAll(arguments)
    }
  }
}
