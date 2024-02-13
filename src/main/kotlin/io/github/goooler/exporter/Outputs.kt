package io.github.goooler.exporter

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.system.exitProcess

internal fun infoOutput(message: Any) {
  terminal.println(TextColors.green(message.toString()))
}

internal fun warnOutput(message: Any) {
  terminal.println(TextColors.yellow(message.toString()))
}

internal fun errorOutput(message: Any): Nothing {
  terminal.println(TextColors.red(message.toString()))
  exitProcess(1)
}

private val terminal = Terminal()
