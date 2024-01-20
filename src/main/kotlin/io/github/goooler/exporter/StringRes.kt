package io.github.goooler.exporter

sealed class TranslatableRes(
  val translatable: Boolean = true,
) {
  abstract val name: String
}

data class StringRes(
  override val name: String,
  val value: String,
) : TranslatableRes() {
  companion object {
    val TAG: String = StringRes::class.java.simpleName
  }
}

data class PluralsRes(
  override val name: String,
  val zero: String,
  val one: String,
  val two: String,
  val few: String,
  val many: String,
  val other: String,
) : TranslatableRes() {
  companion object {
    val TAG: String = PluralsRes::class.java.simpleName
  }
}

typealias StringResColumn = MutableMap<String, StringRes?>
typealias PluralsResColumn = MutableMap<String, PluralsRes?>
