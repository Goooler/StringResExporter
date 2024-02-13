package io.github.goooler.exporter

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.system.exitProcess

internal fun infoOutput(message: Any) {
  output(message.toString(), TextColors.green)
}

internal fun warnOutput(message: Any) {
  output("Warning: $message", TextColors.yellow)
}

internal fun errorOutput(message: Any): Nothing {
  output("Error: $message", TextColors.red)
  exitProcess(1)
}

private fun output(message: String, color: TextColors) {
  terminal.println((color(message)))
}

private val terminal = Terminal()
