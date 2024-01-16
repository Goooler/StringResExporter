package io.github.goooler.exporter

import java.io.InputStream
import java.net.URL

fun requireResource(name: String): URL {
  return Utils::class.java.getResource(name) ?: error("Resource $name not found")
}

fun requireResourceAsStream(name: String): InputStream {
  return Utils::class.java.getResourceAsStream(name) ?: error("Resource $name not found")
}

object Utils
