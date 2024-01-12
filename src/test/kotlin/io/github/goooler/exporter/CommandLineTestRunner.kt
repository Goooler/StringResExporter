package io.github.goooler.exporter

import java.io.InputStream
import java.io.Reader
import java.nio.file.Path
import java.util.concurrent.TimeUnit

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

/*    val stdin = InputStream.nullInputStream()
    process.outputStream.use(stdin::copyTo)*/

    // Give the process some time to complete
    (0..WAIT_INTERVAL_MAX_OCCURRENCES).forEach { _ ->
      if (process.isAlive) {
        // Check regularly whether the ktlint command has finished to speed up the unit testing
        process.waitFor(WAIT_INTERVAL_DURATION, TimeUnit.MILLISECONDS)
      }
    }

    // Get the output from the process before destroying it as otherwise the streams are not collected completely
    val output = process.inputStream.bufferedReader().use(Reader::readLines)
    val error = process.errorStream.bufferedReader().use(Reader::readLines)

    if (process.isAlive) {
      process.destroyForcibly()
    } else {
      val exitCode = process.exitValue()
      if (exitCode != 0) {
        error.forEach(::println)
        throw IllegalStateException("Process exited with code $exitCode")
      }
    }
  }

  companion object {
    private const val WAIT_INTERVAL_DURATION = 100L
    private const val WAIT_INTERVAL_MAX_OCCURRENCES = 10
    private val cliPath = System.getProperty("CLI_PATH") ?: error("CLI_PATH must not be null.")
    private val isWindows = System.getProperty("os.name").startsWith("Windows")

    private fun cliCommand(vararg arguments: String): String = buildList {
      add("java")
      add("-jar")
      add(cliPath)
      addAll(arguments)
    }.joinToString(separator = " ")
  }
}
