package io.github.goooler.exporter

import java.io.File
import java.io.FileOutputStream
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.jdom2.Element
import org.jdom2.input.SAXBuilder

fun main() {
  val folders = listOf(
    "values",
    "values-zh-rCN",
  )

  val workbook = HSSFWorkbook()
  val sheet = workbook.createSheet("Sheet1")

  val defaultColumn: StringResColumn = mutableMapOf()
  val columns = mutableListOf<StringResColumn>()
  val firstRow = sheet.createRow(0).apply {
    createCell(0).setCellValue("key")
  }

  folders.forEachIndexed { index, folder ->
    val inputStream = StringRes::class.java.getResourceAsStream("/$folder/strings.xml")
    val elements = SAXBuilder().build(inputStream).rootElement.children

    columns += if (folder == "values") {
      fillNewColumn(defaultColumn, elements)
    } else {
      val emptyColumn: StringResColumn = defaultColumn.mapValues { null }.toMutableMap()
      fillNewColumn(emptyColumn, elements)
    }
    firstRow.createCell(index + 1).setCellValue(folder)
  }

  columns.forEachIndexed { columnIndex, column ->
    column.entries.forEachIndexed { rowIndex, stringRes ->
      val realRowIndex = rowIndex + 1
      if (columnIndex == 0) {
        sheet.createRow(realRowIndex).createCell(0).setCellValue(stringRes.key)
      }
      sheet.getRow(realRowIndex).createCell(columnIndex + 1)
        .setCellValue(stringRes.value?.value.orEmpty())
    }
  }

  FileOutputStream(File("./output.xls")).use { fos ->
    workbook.use { it.write(fos) }
  }
}

private fun fillNewColumn(
  column: StringResColumn,
  elements: List<Element>,
): StringResColumn {
  elements.forEach { element ->
    val stringRes = StringRes(
      name = element.getAttributeValue("name"),
      value = element.text,
    )
    column[stringRes.name] = stringRes
  }
  return column
}

typealias StringResColumn = MutableMap<String, StringRes?>

data class StringRes(
  val name: String,
  val value: String,
  val translatable: Boolean = true,
)
