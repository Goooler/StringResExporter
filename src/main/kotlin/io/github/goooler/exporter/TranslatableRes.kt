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

data class ArrayRes(
  override val name: String,
  val values: List<String>,
) : TranslatableRes() {
  companion object {
    val TAG: String = ArrayRes::class.java.simpleName
  }
}

typealias ResColumn<T> = MutableMap<String, T?>
