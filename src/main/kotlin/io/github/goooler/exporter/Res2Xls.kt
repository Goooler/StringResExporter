package io.github.goooler.exporter

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.inputStream
import kotlin.io.path.listDirectoryEntries
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.jdom2.Element
import org.jdom2.input.SAXBuilder

const val STRING_RES_SHEET = "String"

fun res2xls(inputPath: String, outputPath: String) {
  val workbook = HSSFWorkbook()
  val sheet = workbook.createSheet(STRING_RES_SHEET)

  val defaultColumn: StringResColumn = mutableMapOf()
  val columns = mutableListOf<StringResColumn>()
  val firstRow = sheet.createRow(0).apply {
    createCell(0).setCellValue("key")
  }

  Paths.get(inputPath).listDirectoryEntries("values*").asSequence()
    .sorted()
    .map {
      it.resolve("strings.xml")
    }
    .filter {
      it.isRegularFile() && it.exists()
    }
    .forEachIndexed { index, path ->
      val elements = SAXBuilder().build(path.inputStream()).rootElement.children

      val folderName = path.parent.name
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

  val outputFile = File(outputPath, "output.xls")
  FileOutputStream(outputFile).use { fos ->
    workbook.use { it.write(fos) }
  }

  println("$SUCCESS_OUTPUT ${outputFile.absolutePath}")
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
