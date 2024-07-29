package io.github.goooler.exporter

import java.io.InputStream
import java.net.URL

fun requireResource(name: String): URL {
  return UnitClass.getResource(name) ?: error("Resource $name not found")
}

fun requireResourceAsStream(name: String): InputStream {
  return UnitClass.getResourceAsStream(name) ?: error("Resource $name not found")
}

private val UnitClass = Unit::class.java
