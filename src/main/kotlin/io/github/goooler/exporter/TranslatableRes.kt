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
) : TranslatableRes() {
  val values: MutableMap<String, String> = mutableMapOf(
    "zero" to "",
    "one" to "",
    "two" to "",
    "few" to "",
    "many" to "",
    "other" to "",
  )

  companion object {
    val TAG: String = PluralsRes::class.java.simpleName
  }
}

typealias StringResColumn = MutableMap<String, StringRes?>
typealias PluralsResColumn = MutableMap<String, PluralsRes?>
