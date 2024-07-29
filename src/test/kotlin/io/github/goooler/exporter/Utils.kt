package io.github.goooler.exporter

import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.toPath

fun requireResourceAsPath(name: String): Path {
  return UnitClass.getResource(name)?.toURI()?.toPath() ?: error("Resource $name not found.")
}

fun requireResourceAsStream(name: String): InputStream {
  return UnitClass.getResourceAsStream(name) ?: error("Resource $name not found.")
}

private val UnitClass = Unit::class.java
