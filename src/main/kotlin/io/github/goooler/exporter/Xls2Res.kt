package io.github.goooler.exporter

import java.io.File
import java.nio.file.Paths
import kotlin.io.path.inputStream
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter

fun xls2res(inputPath: String, outputPath: String) {
  val workbook = WorkbookFactory.create(Paths.get(inputPath).inputStream())
  writeStrings(workbook, outputPath)
  writePlurals(workbook, outputPath)
  println("$SUCCESS_OUTPUT $outputPath")
}

internal fun writeStrings(workbook: Workbook, outputPath: String) {
  val stringSheet = workbook.getSheet(StringRes.TAG)
  val stringResMap = mutableMapOf<String, MutableList<StringRes>>()

  stringSheet.rowIterator().asSequence().drop(1).forEach { row ->
    val key = row.getCell(0).stringCellValue
    row.cellIterator().asSequence().drop(1).forEachIndexed { index, cell ->
      val folderName = stringSheet.getRow(0).getCell(index + 1).stringCellValue
      val value = cell.stringCellValue
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
  val pluralsSheet = workbook.getSheet(PluralsRes.TAG)
  val pluralsResMap = mutableMapOf<String, MutableList<PluralsRes>>()

  pluralsSheet.rowIterator().asSequence().drop(1).forEachIndexed { rowIndex, row ->
    row.cellIterator().asSequence().drop(2).forEachIndexed { index, cell ->
      val folderName = pluralsSheet.getRow(0).getCell(index + 2).stringCellValue
      val quantity = row.getCell(1).stringCellValue.orEmpty()
      if (rowIndex % 6 == 0) {
        val key = row.getCell(0).stringCellValue
        val pluralsRes = PluralsRes(key).apply {
          values[quantity] = cell.stringCellValue.orEmpty()
        }
        pluralsResMap.getOrPut(folderName) {
          mutableListOf()
        }.add(pluralsRes)
      } else {
        val pluralsRes = pluralsResMap.getValue(folderName)[rowIndex / 6]
        pluralsRes.values[quantity] = cell.stringCellValue.orEmpty()
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
