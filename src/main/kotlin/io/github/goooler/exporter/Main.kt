package io.github.goooler.exporter

fun main(args: Array<String>) {
  val (command, inputPath, outputPath) = args
  when (command) {
    "res2xml" -> res2xml(inputPath, outputPath)
    else -> error("Unknown command: $command")
  }
}
