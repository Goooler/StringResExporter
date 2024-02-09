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

    fun StringRes.map(): StringRes {
      return copy(value = "")
    }
  }
}

data class PluralsRes(
  override val name: String,
  val values: MutableMap<String, String> = DEFAULT_VALUES.toMutableMap(),
) : TranslatableRes() {
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

    fun PluralsRes.map(): PluralsRes {
      return copy(values = DEFAULT_VALUES.toMutableMap())
    }
  }
}

data class ArrayRes(
  override val name: String,
  val values: List<String>,
) : TranslatableRes() {
  companion object {
    val TAG: String = ArrayRes::class.java.simpleName

    fun ArrayRes.map(): ArrayRes {
      val emptyContentValues = List(values.size) { "" }
      return copy(values = emptyContentValues)
    }
  }
}
