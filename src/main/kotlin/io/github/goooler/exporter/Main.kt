package io.github.goooler.exporter

import io.github.goooler.internal.InternalLogger

fun main(vararg args: String) {
  val (command, inputPath, outputPath) = args
  when (command) {
    "--res2xls" -> res2xls(inputPath, outputPath)
    "--xls2res" -> xls2res(inputPath, outputPath)
    else -> error("Unknown command: $command")
  }
}

val logger = InternalLogger

const val SUCCESS_OUTPUT = "Convert finished, output path:"
