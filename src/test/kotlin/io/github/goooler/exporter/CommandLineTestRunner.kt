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
      error("Error occurred when running command line: $err")
    }
    if (!out.startsWith(SUCCESS_OUTPUT)) {
      error("Output is not correct: $out")
    }
  }

  companion object {
    private val cliPath = System.getProperty("CLI_PATH") ?: error("CLI_PATH must not be null.")

    private fun cliCommand(vararg arguments: String) = buildList {
      addAll(listOf("java", "-Dlog4j2.loggerContextFactory=org.apache.logging.log4j.simple.SimpleLoggerContextFactory", "-jar", cliPath))
      addAll(arguments)
    }
  }
}
