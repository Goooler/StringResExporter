package io.github.goooler.exporter

import java.io.File
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.io.path.inputStream
import org.apache.poi.ss.usermodel.WorkbookFactory

fun xls2res(inputPath: String, outputPath: String) {
  val workbook = WorkbookFactory.create(Paths.get(inputPath).inputStream())
  val sheet = workbook.getSheet(STRING_RES_SHEET)
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

  val dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  val transformer = TransformerFactory.newInstance().newTransformer()

  transformer.setOutputProperty(OutputKeys.INDENT, "yes")
  @Suppress("HttpUrlsUsage")
  transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

  stringResMap.forEach { (folderName, stringResList) ->
    val doc = dBuilder.newDocument()
    val rootElement = doc.createElement("resources")

    stringResList.forEach { stringRes ->
      val stringElement = doc.createElement("string").apply {
        setAttribute("name", stringRes.name)
        textContent = stringRes.value
      }
      rootElement.appendChild(stringElement)
    }

    doc.appendChild(rootElement)

    val outputFile = File(outputPath, "$folderName/strings.xml").apply {
      parentFile.mkdirs()
    }

    transformer.transform(DOMSource(doc), StreamResult(outputFile))
  }

  println("$SUCCESS_OUTPUT $outputPath")
}
