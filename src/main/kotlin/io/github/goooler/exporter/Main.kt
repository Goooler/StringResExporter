package io.github.goooler.exporter

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import kotlin.io.path.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.jdom2.Element
import org.jdom2.input.SAXBuilder

fun main(args: Array<String>) {
  val args = arrayOf("/Users/goooler/StudioProjects/lawnchair/res")

  val errorMessage = "Please input the res folder path"
  check(args.size == 1) {
    errorMessage
  }
  val resFolder = Paths.get(args.first())
  check(resFolder.exists() && resFolder.isDirectory()) {
    errorMessage
  }

  val folders = resFolder.listDirectoryEntries("values*").sorted()

  val workbook = HSSFWorkbook()
  val sheet = workbook.createSheet("Sheet1")

  val defaultColumn: StringResColumn = mutableMapOf()
  val columns = mutableListOf<StringResColumn>()
  val firstRow = sheet.createRow(0).apply {
    createCell(0).setCellValue("key")
  }

  folders.forEachIndexed { index, folder ->
    val xmlFile = folder.resolve("strings.xml").takeIf { it.exists() } ?: return@forEachIndexed
    val inputStream = xmlFile.inputStream()
    val elements = SAXBuilder().build(inputStream).rootElement.children

    val folderName = folder.name
    columns += if (folderName == "values") {
      fillNewColumn(defaultColumn, elements)
    } else {
      val emptyColumn: StringResColumn = defaultColumn.mapValues { null }.toMutableMap()
      fillNewColumn(emptyColumn, elements)
    }
    firstRow.createCell(index + 1).setCellValue(folderName)
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
    if (element.name != "string") return@forEach
    val key = element.getAttributeValue("name") ?: return@forEach
    val stringRes = StringRes(
      name = key,
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
