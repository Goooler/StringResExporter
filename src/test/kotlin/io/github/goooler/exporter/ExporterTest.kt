package io.github.goooler.exporter

import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.containsExactly
import assertk.assertions.isTrue
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyToRecursively
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.jdom2.input.SAXBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ExporterTest {

  @Test
  fun exportAndImport(@TempDir tempDir: Path) {
    val importedRes = tempDir.resolve("resInput")
    Paths.get(requireResource("/res").toURI()).copyToRecursively(importedRes)
    CommandLineTestRunner(
      tempDir,
      "--res2xls",
      importedRes.absolutePathString(),
      tempDir.absolutePathString(),
    ).run()

    val exportedXls = tempDir.resolve("output.xls")
    assertThat(exportedXls.exists()).isTrue()
    assertThat(exportedXls.isRegularFile()).isTrue()
    validateXlsContent(exportedXls)

    val exportedRes = tempDir.resolve("resOutput")
    CommandLineTestRunner(
      tempDir,
      "--xls2res",
      exportedXls.absolutePathString(),
      exportedRes.absolutePathString(),
    ).run()

    assertThat(exportedRes.exists()).isTrue()
    assertThat(exportedRes.listDirectoryEntries().size == 3).isTrue()
    validateResContent(importedRes, exportedRes)
  }

  private fun validateXlsContent(exportedXls: Path) {
    val workbook = WorkbookFactory.create(exportedXls.inputStream())
    val sheetContent = workbook.getSheet(STRING_RES_SHEET)
      .asSequence()
      .flatMap {
        buildList {
          for (i in 0 until it.lastCellNum) {
            add(it.getCell(i))
          }
        }
      }.map {
        it.stringCellValue.orEmpty()
      }.toList()
    val expectedContent = arrayOf(
      "key", "values", "values-it", "values-zh-rCN",
      "first", "first", "primo", "",
      "second", "second", "", "第二",
      "third", "third", "", "",
      "forth", "forth", "quarto", "",
      "fifth", "fifth", "", "第五",
    )
    assertThat(sheetContent).containsExactly(*expectedContent)
  }

  private fun validateResContent(importedRes: Path, exportedRes: Path) {
    val expected = parseRes(importedRes).toTypedArray()
    val actual = parseRes(exportedRes).toTypedArray()

    assertThat(expected).containsAtLeast(*actual)
  }

  private fun parseRes(resFolder: Path): List<StringRes> {
    val parsed = mutableListOf<StringRes>()
    resFolder.listDirectoryEntries().asSequence()
      .sorted()
      .forEach { subFolder ->
        parsed += SAXBuilder().build(
          subFolder.resolve("strings.xml").inputStream(),
        ).rootElement.children.asSequence()
          .map {
            it.toStringResOrNull()
          }
          .filterIsInstance<StringRes>()
          .filter {
            it.value.isNotEmpty()
          }
      }
    return parsed
  }

  @OptIn(ExperimentalPathApi::class)
  private fun Path.copyToRecursively(target: Path) {
    copyToRecursively(target, followLinks = true, overwrite = true)
  }
}
