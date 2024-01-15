package io.github.goooler.exporter

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

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
      val elements = xml2Elements(path.toFile())
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

internal fun Element.toStringRes(): StringRes? {
  if (tagName != "string") return null
  if (!hasAttribute("name")) return null
  val key = getAttribute("name")
  return StringRes(
    name = key,
    value = textContent,
  )
}

internal fun xml2Elements(xmlFile: File): List<Element> {
  val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile).apply {
    documentElement.normalize()
  }
  val nList: NodeList = doc.documentElement.childNodes
  val elements = mutableListOf<Element>()
  for (temp in 0 until nList.length) {
    val nNode: Node = nList.item(temp)
    if (nNode.nodeType == Node.ELEMENT_NODE) {
      val eElement: Element = nNode as Element
      elements.add(eElement)
    }
  }
  return elements
}

private fun fillNewColumn(
  column: StringResColumn,
  elements: List<Element>,
): StringResColumn {
  elements.forEach { element ->
    val stringRes = element.toStringRes() ?: return@forEach
    column[stringRes.name] = stringRes
  }
  return column
}
