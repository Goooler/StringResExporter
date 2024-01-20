package io.github.goooler.exporter

import java.io.File
import java.nio.file.Paths
import kotlin.io.path.inputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter

fun xls2res(inputPath: String, outputPath: String) {
  val workbook = WorkbookFactory.create(Paths.get(inputPath).inputStream())
  val sheet = workbook.getSheet(StringRes.TAG)
  val stringResMap = mutableMapOf<String, MutableList<StringRes>>()

  sheet.rowIterator().asSequence().drop(1).forEach { row ->
    val key = row.getCell(0).stringCellValue
    row.cellIterator().asSequence().drop(1).forEachIndexed { index, cell ->
      val folderName = sheet.getRow(0).getCell(index + 1).stringCellValue
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

  println("$SUCCESS_OUTPUT $outputPath")
}
