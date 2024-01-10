package io.github.goooler.exporter

import java.io.File
import java.io.FileOutputStream
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.jdom2.Element
import org.jdom2.input.SAXBuilder


fun main() {
  val folders = listOf(
    "values",
    "values-zh-rCN",
  )

  val workbook = HSSFWorkbook()
  val sheet = workbook.createSheet("Sheet1")

  val defaultResColumn: StringResColumn = mutableMapOf()
  val resColumns = mutableListOf<StringResColumn>()
  val firstRow = sheet.createRow(0).apply {
    createCell(0, CellType.STRING).setCellValue("key")
  }

  folders.forEachIndexed { index, folder ->
    val inputStream = StringRes::class.java.getResourceAsStream("/$folder/strings.xml")
    val elements = SAXBuilder().build(inputStream).rootElement.children

    resColumns += if (folder == "values") {
      fillNewColumn(defaultResColumn, elements)
    } else {
      val emptyColumn: StringResColumn = defaultResColumn.mapValues { null }.toMutableMap()
      fillNewColumn(emptyColumn, elements)
    }
    firstRow.createCell(index + 1, CellType.STRING).setCellValue(folder)
  }

  resColumns.forEachIndexed { columnIndex, column ->
    column.entries.forEachIndexed { rowIndex, entry ->
      if (columnIndex == 0) {
        sheet.createRow(rowIndex + 1).createCell(0, CellType.STRING).setCellValue(entry.key)
      }
      sheet.getRow(rowIndex + 1).createCell(columnIndex + 1, CellType.STRING)
        .setCellValue(entry.value?.value.orEmpty())
    }
  }

  FileOutputStream(File("./output.xls")).use { fos ->
    workbook.write(fos)
    workbook.close()
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
