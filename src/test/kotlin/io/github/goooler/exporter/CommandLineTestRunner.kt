package io.github.goooler.exporter

import java.nio.charset.StandardCharsets

class CommandLineTestRunner(
  private val converter: String,
  private val inputPath: String,
  private val outputPath: String,
) : Runnable {

  override fun run() {
    val process = ProcessBuilder(cliCommand(converter, inputPath, outputPath)).start()
    val exitCode = process.waitFor()

    val err = process.errorStream.readBytes().toString(StandardCharsets.UTF_8)
    val out = process.inputStream.readBytes().toString(StandardCharsets.UTF_8)

    if (exitCode != 0 || err.isNotEmpty()) {
      runningError("Error occurred when running command line: $err")
    }
    if (!out.contains(SUCCESS_OUTPUT)) {
      runningError("Output is not correct: $out")
    }
  }

  private fun runningError(message: String) {
    throw CommandLineException(message)
  }

  private class CommandLineException(message: String) : IllegalStateException(message)

  companion object {
    private val cliPath = System.getProperty("CLI_PATH") ?: error("CLI_PATH must not be null.")

    private fun cliCommand(vararg arguments: String) = buildList {
      addAll(listOf("java", "-jar", cliPath))
      addAll(arguments)
    }
  }
}
