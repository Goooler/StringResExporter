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

  val defaultStringColumn: ResColumn<StringRes> = mutableMapOf()
  val defaultPluralsColumn: ResColumn<PluralsRes> = mutableMapOf()
  val stringColumns = mutableListOf<ResColumn<StringRes>>()
  val pluralsColumns = mutableListOf<ResColumn<PluralsRes>>()

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
        val newStringColumn: ResColumn<StringRes> = defaultStringColumn.mapValues {
          it.value.copy(value = "")
        }.toMutableMap()
        val newPluralsColumn: ResColumn<PluralsRes> = defaultPluralsColumn.mapValues {
          it.value.copy(values = PluralsRes.DEFAULT_VALUES.toMutableMap())
        }.toMutableMap()
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
    column.values.forEachIndexed { rowIndex, stringRes ->
      val realRowIndex = rowIndex + 1
      if (columnIndex == 0) {
        val key = stringRes.name
        check(key.isNotEmpty()) { "Default string res keys can't be null" }
        stringSheet.createRow(realRowIndex).createCell(0).setCellValue(key)
      }
      stringSheet.getRow(realRowIndex).createCell(columnIndex + 1)
        .setCellValue(stringRes.value)
    }
  }

  pluralsColumns.forEachIndexed { columnIndex, column ->
    column.values.forEachIndexed { rowIndex, pluralsRes ->
      val start = rowIndex * 6 + 1
      val end = start + 6
      val pluralsValues = pluralsRes.values
      for (i in start until end) {
        val row = pluralsSheet.getRow(i) ?: pluralsSheet.createRow(i)
        if (columnIndex == 0) {
          check(pluralsValues.isNotEmpty()) { "Default plurals res values can't be null" }
          // Write key only once for a plurals res.
          if (i == start) {
            row.createCell(0).setCellValue(pluralsRes.name)
          } else {
            row.createCell(0).setCellValue("")
          }
          val quantity = pluralsValues.entries.toList()[i - start]
          row.createCell(1).setCellValue(quantity.key)
          row.createCell(2).setCellValue(quantity.value)
        } else {
          val value = pluralsValues.values.toList()[i - start]
          row.createCell(columnIndex + 2).setCellValue(value)
        }
      }
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
  stringColumn: ResColumn<StringRes>,
  pluralsColumn: ResColumn<PluralsRes>,
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
