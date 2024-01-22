package io.github.goooler.exporter

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.jdom2.Element
import org.jdom2.input.SAXBuilder

fun res2xls(inputPath: String, outputPath: String) {
  val workbook = HSSFWorkbook()
  val stringSheet = workbook.createSheet(StringRes.TAG).apply {
    createRow(0).createCell(0).setCellValue("key")
  }
  val pluralsSheet = workbook.createSheet(PluralsRes.TAG).apply {
    val firstRow = createRow(0)
    firstRow.createCell(0).setCellValue("key")
    firstRow.createCell(1).setCellValue("quantity")
  }

  val defaultStringColumn: StringResColumn = mutableMapOf()
  val defaultPluralsColumn: PluralsResColumn = mutableMapOf()
  val stringColumns = mutableListOf<StringResColumn>()
  val pluralsColumns = mutableListOf<PluralsResColumn>()

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
      if (folderName == "values") {
        fillNewColumn(true, elements, defaultStringColumn, defaultPluralsColumn)
        stringColumns += defaultStringColumn
        pluralsColumns += defaultPluralsColumn
      } else {
        val newStringColumn: StringResColumn = defaultStringColumn.mapValues { null }.toMutableMap()
        val newPluralsColumn: PluralsResColumn = defaultPluralsColumn.mapValues { null }.toMutableMap()
        fillNewColumn(false, elements, newStringColumn, newPluralsColumn)
        stringColumns += newStringColumn
        pluralsColumns += newPluralsColumn
      }
      // key, value, value-zh-rCN...
      stringSheet.first().createCell(index + 1).setCellValue(folderName)
      // key, quantity, value, value-zh-rCN...
      pluralsSheet.first().createCell(index + 2).setCellValue(folderName)
    }

  stringColumns.forEachIndexed { columnIndex, column ->
    column.entries.forEachIndexed { rowIndex, stringRes ->
      val realRowIndex = rowIndex + 1
      if (columnIndex == 0) {
        stringSheet.createRow(realRowIndex).createCell(0).setCellValue(stringRes.key)
      }
      stringSheet.getRow(realRowIndex).createCell(columnIndex + 1)
        .setCellValue(stringRes.value?.value.orEmpty())
    }
  }

  val outputFile = File(outputPath, "output.xls")
  FileOutputStream(outputFile).use { fos ->
    workbook.use { it.write(fos) }
  }

  println("$SUCCESS_OUTPUT ${outputFile.absolutePath}")
}

internal fun Element.toStringResOrNull(): StringRes? {
  if (name != "string") return null
  val key = getAttributeValue("name") ?: return null
  return StringRes(
    name = key,
    value = text,
  )
}

internal fun Element.toPluralsResOrNull(): PluralsRes? {
  if (name != "plurals") return null
  val key = getAttributeValue("name") ?: return null
  val pluralsRes = PluralsRes(key)
  children.forEach {
    val quantity = it.getAttributeValue("quantity") ?: return@forEach
    pluralsRes.values[quantity] = it.text
  }
  return pluralsRes
}

private fun fillNewColumn(
  fillDefault: Boolean,
  elements: List<Element>,
  stringColumn: StringResColumn,
  pluralsColumn: PluralsResColumn,
) {
  elements.forEach { element ->
    val res = element.toStringResOrNull() ?: element.toPluralsResOrNull()
    when (res) {
      is StringRes -> {
        if (fillDefault || stringColumn.containsKey(res.name)) {
          stringColumn[res.name] = res
        }
      }
      is PluralsRes -> {
        if (fillDefault || pluralsColumn.containsKey(res.name)) {
          pluralsColumn[res.name] = res
        }
      }
      null -> Unit
    }
  }
}
