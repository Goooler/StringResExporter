package io.github.goooler.exporter

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.junit.jupiter.api.Test

class MappingTest {

  @Test
  fun parseString() {
    val stringResList = parseElements().mapNotNull {
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
    val pluralsResList = parseElements().mapNotNull {
      it.toPluralsResOrNull()
    }
    val pluralsRes = pluralsResList.single()
    assertThat(pluralsRes.toString()).isEqualTo("PluralsRes(name=apples, values={zero=, one=mela, two=, few=, many=mele, other=mele})")
  }

  @Test
  fun parseArray() {
    val arrayResList = parseElements().mapNotNull {
      it.toArrayResOrNull()
    }
    val arrayRes = arrayResList.single()
    assertThat(arrayRes.toString()).isEqualTo("ArrayRes(name=colors, values=[rosso, verde, blu, giallo])")
  }

  @Test
  fun parseCellString() {
    val row = HSSFWorkbook().createSheet("test").createRow(0)
    row.createCell(0).setCellValue(" Hello${NBSP}World ")
    row.createCell(1).setCellValue(false)

    assertThat(row.getCell(0).stringValue).isEqualTo("Hello World")
    assertFailure {
      row.getCell(1).stringValue
    }.hasMessage("Error: Cell in sheet test row 0 and column 1 is not a string.")
  }

  private fun parseElements(resPath: String = "/res/values-it/strings.xml"): List<Element> {
    val inputStream = requireResourceAsStream(resPath)
    return SAXBuilder().build(inputStream).rootElement.children
  }
}
