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
    val out = process.inputStream.readBytes().toString(StandardCharsets.UTF_8).let {
      // If ANSI escape codes are not supported, remove them from the output
      if (System.console() == null) it.replace("\u001B\\[.*?m".toRegex(), "") else it
    }
    if (exitCode != 0 || err.isNotEmpty()) {
      error("Error occurred when running command line: $err")
    }
    if (!out.startsWith(SUCCESS_OUTPUT)) {
      error("Output is not correct: $out")
    }
  }

  companion object {
    private val isWindows = System.getProperty("os.name").startsWith("Windows")
    private val cliPath = System.getProperty("CLI_PATH") ?: error("CLI_PATH must not be null.")

    private fun cliCommand(vararg arguments: String) = buildList {
      // Binary Jar is not executable on Windows.
      if (isWindows) {
        addAll(listOf("java", "-jar"))
      }
      add(cliPath)
      addAll(arguments)
    }
  }
}
