package io.github.goooler.exporter

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

fun main(vararg args: String) {
  val (command, inputPath, outputPath) = args
  when (command) {
    "--res2xls" -> res2xls(inputPath, outputPath)
    "--xls2res" -> xls2res(inputPath, outputPath)
    else -> error("Unknown command: $command")
  }
}

val logger: Logger = LogManager.getLogger("Main")

const val SUCCESS_OUTPUT = "Convert finished, output path:"
