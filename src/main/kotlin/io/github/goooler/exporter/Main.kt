package io.github.goooler.exporter

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.options.versionOption

fun main(vararg args: String) = ExporterCommand().main(args.toList())

internal const val SUCCESS_OUTPUT = "Convert finished, output path:"

private class ExporterCommand private constructor() : CliktCommand(
  name = BuildConfig.CLI_NAME,
  help = "Facilitate the export and import of string resources between Android projects and XLS files.",
) {
  private val converter by converterType()
  private val inputPath by argument(help = "The input path of the resources or XLS file.")
  private val outputPath by argument(help = "The output path of the resources or XLS file.")

  override fun run() {
    when (converter) {
      ConverterType.Res2Xls -> res2xls(inputPath, outputPath)
      ConverterType.Xls2Res -> xls2res(inputPath, outputPath)
    }
  }

  companion object {
    operator fun invoke(): ExporterCommand = ExporterCommand()
      .versionOption(BuildConfig.VERSION_NAME, names = setOf("--version", "-v"))
  }
}

private enum class ConverterType {
  Res2Xls,
  Xls2Res,
}

private fun ParameterHolder.converterType(): OptionWithValues<ConverterType, ConverterType, String> {
  return option(help = "The command to convert resources to XLS or XLS to resources.")
    .switch(
      "--res2xls" to ConverterType.Res2Xls,
      "--xls2res" to ConverterType.Xls2Res,
    )
    .default(ConverterType.Res2Xls)
}
