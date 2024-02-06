package io.github.goooler.exporter

sealed class TranslatableRes(
  val translatable: Boolean = true,
) {
  abstract val name: String

  abstract fun copy(): TranslatableRes
}

data class StringRes(
  override val name: String,
  val value: String,
) : TranslatableRes() {

  override fun copy(): StringRes {
    return copy(value = "")
  }

  companion object {
    val TAG: String = StringRes::class.java.simpleName
  }
}

data class PluralsRes(
  override val name: String,
  val values: MutableMap<String, String> = DEFAULT_VALUES.toMutableMap(),
) : TranslatableRes() {

  override fun copy(): PluralsRes {
    return copy(values = DEFAULT_VALUES.toMutableMap())
  }

  companion object {
    private val DEFAULT_VALUES = mapOf(
      "zero" to "",
      "one" to "",
      "two" to "",
      "few" to "",
      "many" to "",
      "other" to "",
    )
    val TAG: String = PluralsRes::class.java.simpleName
  }
}

data class ArrayRes(
  override val name: String,
  val values: List<String>,
) : TranslatableRes() {

  override fun copy(): ArrayRes {
    val emptyContentValues = List(values.size) { "" }
    return copy(values = emptyContentValues)
  }

  companion object {
    val TAG: String = ArrayRes::class.java.simpleName
  }
}
