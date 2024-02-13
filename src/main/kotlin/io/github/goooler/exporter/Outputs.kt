package io.github.goooler.exporter

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.system.exitProcess

internal fun infoOutput(message: Any) {
  Terminal().println(TextColors.green(message.toString()))
}

internal fun warnOutput(message: Any) {
  Terminal().println(TextColors.yellow(message.toString()))
}

internal fun errorOutput(message: Any): Nothing {
  Terminal().println(TextColors.red(message.toString()))
  exitProcess(1)
}
