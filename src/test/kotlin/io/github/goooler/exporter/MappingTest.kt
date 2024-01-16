package io.github.goooler.exporter

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.jdom2.input.SAXBuilder
import org.junit.jupiter.api.Test

class MappingTest {

  @Test
  fun parseString() {
    val inputStream = requireResourceAsStream("/res/values-it/strings.xml")
    val stringResList = SAXBuilder().build(inputStream).rootElement.children.mapNotNull {
      it.toStringResOrNull()
    }
    val actual = arrayOf(
      StringRes("first", "primo"),
      StringRes("forth", "quarto"),
      StringRes("seventh", "settimo"),
    )
    assertThat(stringResList).containsExactly(*actual)
  }

  @Test
  fun parsePlurals() {
    val inputStream = requireResourceAsStream("/res/values-it/strings.xml")
    val pluralsResList = SAXBuilder().build(inputStream).rootElement.children.mapNotNull {
      it.toPluralsResOrNull()
    }
    assertThat(pluralsResList.single().toString())
      .isEqualTo("PluralsRes(name=apples, zero=, one=mela, two=, few=, many=mele, other=mele)")
  }
}
