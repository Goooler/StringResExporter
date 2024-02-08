package io.github.goooler.internal

import java.util.logging.LogManager
import java.util.logging.Logger

object InternalLogger {
  private val logger: Logger = LogManager.getLogManager().getLogger("internal")

  fun info(message: String) {
    logger.info(message)
  }
}
