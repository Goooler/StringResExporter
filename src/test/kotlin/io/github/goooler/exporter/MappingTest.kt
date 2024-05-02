package io.github.goooler.exporter

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.ginsberg.junit.exit.ExpectSystemExitWithStatus
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
      StringRes("countdown", "\n    <xliff:g id=\"time\" example=\"5 days\">%1\$s</xliff:g> fino alle vacanze\n  "),
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
  @ExpectSystemExitWithStatus(1)
  fun parseCellString() {
    val row = HSSFWorkbook().createSheet("test").createRow(0)
    row.createCell(0).setCellValue(" Hello${NBSP}World ")
    assertThat(row.getCell(0).stringValue).isEqualTo("Hello World")

    row.createCell(1).setCellValue(false)
    // Incorrect cell value will cause the process to exit abnormally by invoking `outputError`.
    row.getCell(1).stringValue
  }

  private fun parseElements(resPath: String = "/res/values-it/strings.xml"): List<Element> {
    val inputStream = requireResourceAsStream(resPath)
    return SAXBuilder().build(inputStream).rootElement.children
  }
}
