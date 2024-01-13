package io.github.goooler.exporter

fun main(args: Array<String>) {
  val (command, inputPath, outputPath) = args
  when (command) {
    "--res2xls" -> res2xls(inputPath, outputPath)
    "--xls2res" -> xls2res(inputPath, outputPath)
    else -> error("Unknown command: $command")
  }
}

const val SUCCESS_OUTPUT = "Convert finished, output path:"
