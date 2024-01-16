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

const val STRING_RES_SHEET = "String"

fun res2xls(inputPath: String, outputPath: String) {
  val workbook = HSSFWorkbook()
  val sheet = workbook.createSheet(STRING_RES_SHEET)

  val defaultStringColumn: StringResColumn = mutableMapOf()
  val defaultPluralsColumn: PluralsResColumn = mutableMapOf()
  val stringColumns = mutableListOf<StringResColumn>()
  val pluralsColumns = mutableListOf<PluralsResColumn>()
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
      firstRow.createCell(index + 1).setCellValue(folderName)
    }

  stringColumns.forEachIndexed { columnIndex, column ->
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
  val quantities = listOf("zero", "one", "two", "few", "many", "other")
  val quantityValues = quantities.map { quantity ->
    children.firstOrNull {
      it.getAttributeValue("quantity") == quantity
    }?.text.orEmpty()
  }
  return PluralsRes(
    name = key,
    zero = quantityValues[0],
    one = quantityValues[1],
    two = quantityValues[2],
    few = quantityValues[3],
    many = quantityValues[4],
    other = quantityValues[5],
  )
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
