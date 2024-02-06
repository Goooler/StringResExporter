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
  val values: MutableMap<String, String> = DEFAULT_VALUES.toMutableMap(),
) : TranslatableRes() {
  companion object {
    val TAG: String = PluralsRes::class.java.simpleName
    val DEFAULT_VALUES = mapOf(
      "zero" to "",
      "one" to "",
      "two" to "",
      "few" to "",
      "many" to "",
      "other" to "",
    )
  }
}
