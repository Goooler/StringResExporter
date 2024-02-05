package io.github.goooler.exporter

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.inputStream
import org.apache.poi.EmptyFileException
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.FileMagic
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter

fun xls2res(inputPath: String, outputPath: String) {
  if (!inputPath.endsWith(".xls") && !inputPath.endsWith(".xlsx")) {
    error("Only support converting res from .xls or .xlsx file.")
  }

  val workbook = Paths.get(inputPath).toWorkbook()
  writeStrings(workbook, outputPath)
  writePlurals(workbook, outputPath)
  println("$SUCCESS_OUTPUT $outputPath")
}

internal fun writeStrings(workbook: Workbook, outputPath: String) {
  val stringSheet = workbook.getSheet(StringRes.TAG) ?: run {
    logger.info("Sheet ${StringRes.TAG} not found.")
    return
  }
  val stringResMap = mutableMapOf<String, MutableList<StringRes>>()

  stringSheet.rowIterator().asSequence().drop(1).forEach { row ->
    if (row.isEmpty()) return@forEach
    val key = row.getCell(0).stringValue
    row.cellIterator().asSequence().drop(1).forEachIndexed { index, cell ->
      val folderName = stringSheet.getRow(0).getCell(index + 1).stringValue
      val value = cell.stringValue
      stringResMap.getOrPut(folderName) {
        mutableListOf()
      }.add(StringRes(key, value))
    }
  }

  stringResMap.forEach { (folderName, stringResList) ->
    val rootElement = Element("resources")
    stringResList.forEach { stringRes ->
      val stringElement = Element("string").apply {
        setAttribute("name", stringRes.name)
        text = stringRes.value
      }
      rootElement.addContent(stringElement)
    }

    val document = Document(rootElement)
    val xmlOutputter = XMLOutputter(Format.getPrettyFormat())
    val outputFile = File(outputPath, "$folderName/strings.xml")
    outputFile.parentFile.mkdirs()
    xmlOutputter.output(document, outputFile.outputStream())
  }
}

internal fun writePlurals(workbook: Workbook, outputPath: String) {
  val pluralsSheet = workbook.getSheet(PluralsRes.TAG) ?: run {
    logger.info("Sheet ${PluralsRes.TAG} not found.")
    return
  }
  val pluralsResMap = mutableMapOf<String, MutableList<PluralsRes>>()

  pluralsSheet.rowIterator().asSequence().drop(1).forEachIndexed { rowIndex, row ->
    if (row.isEmpty()) return@forEachIndexed
    row.cellIterator().asSequence().drop(2).forEachIndexed { index, cell ->
      val folderName = pluralsSheet.getRow(0).getCell(index + 2).stringValue
      val quantity = row.getCell(1).stringValue
      if (rowIndex % 6 == 0) {
        val key = row.getCell(0).stringValue
        val pluralsRes = PluralsRes(key).apply {
          values[quantity] = cell.stringValue
        }
        pluralsResMap.getOrPut(folderName) {
          mutableListOf()
        }.add(pluralsRes)
      } else {
        val pluralsRes = pluralsResMap.getValue(folderName)[rowIndex / 6]
        pluralsRes.values[quantity] = cell.stringValue
      }
    }
  }

  pluralsResMap.forEach { (folderName, pluralsResList) ->
    val rootElement = Element("resources")
    pluralsResList.forEach res@{ pluralsRes ->
      val pluralsElement = Element("plurals").apply {
        var emptyItemCount = 0
        pluralsRes.values.forEach item@{ (quantity, value) ->
          if (value.isEmpty()) {
            emptyItemCount++
            return@item
          }
          val itemElement = Element("item").apply {
            setAttribute("quantity", quantity)
            text = value
          }
          addContent(itemElement)
        }
        if (emptyItemCount == 6) return@res
        setAttribute("name", pluralsRes.name)
      }
      rootElement.addContent(pluralsElement)
    }

    val document = Document(rootElement)
    val xmlOutputter = XMLOutputter(Format.getPrettyFormat())
    val outputFile = File(outputPath, "$folderName/plurals.xml")
    outputFile.parentFile.mkdirs()
    xmlOutputter.output(document, outputFile.outputStream())
  }
}

/**
 * Copy-paste some creating logic from [WorkbookFactory.create]
 */
internal fun Path.toWorkbook(): Workbook {
  val inputStream = FileMagic.prepareToCheckMagic(inputStream())
  val emptyFileCheck = ByteArray(1)
  inputStream.mark(emptyFileCheck.size)
  if (inputStream.read(emptyFileCheck) < emptyFileCheck.size) {
    throw EmptyFileException()
  }
  inputStream.reset()
  return when (FileMagic.valueOf(inputStream)) {
    FileMagic.OLE2 -> HSSFWorkbook(inputStream)
    FileMagic.OOXML -> XSSFWorkbook(inputStream)
    else -> error("Unsupported file type: $this")
  }
}

internal val Cell.stringValue: String
  get() {
    val original = try {
      stringCellValue
    } catch (_: IllegalStateException) {
      error("Cell in sheet ${sheet.sheetName} row $rowIndex and column $columnIndex is not a string.")
    }
    return original.trim().replace(NBSP, SPACE)
  }

internal const val NBSP = '\u00A0'
internal const val SPACE = '\u0020'

private fun Row.isEmpty(): Boolean = all { it.stringCellValue.trim().isEmpty() }
