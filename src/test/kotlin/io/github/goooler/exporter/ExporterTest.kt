package io.github.goooler.exporter

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isTrue
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.exists
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ExporterTest {

  @Test
  fun exportAndImport(@TempDir tempDir: Path) {
    val importedRes = tempDir.resolve("resInput")
    Paths.get(this::class.java.getResource("/res").toURI()).copyToRecursively(importedRes)
    CommandLineTestRunner(
      tempDir,
      "--res2xls",
      importedRes.absolutePathString(),
      tempDir.absolutePathString()
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
    assertThat(exportedRes.listDirectoryEntries().size == 2).isTrue()
  }

  private fun validateXlsContent(exportedXls: Path) {
    val workbook = WorkbookFactory.create(exportedXls.inputStream())
    val sheetContent = workbook.getSheet(STRING_RES_SHEET)
      .asSequence()
      .flatMap {
        listOf(it.getCell(0), it.getCell(1), it.getCell(2))
      }.map {
        it.stringCellValue.orEmpty()
      }.toList()
    val expectedContent = arrayOf(
      "key",
      "values",
      "values-zh-rCN",
      "lib_coil",
      "Coil",
      "",
      "lib_okhttp",
      "OkHttp",
      "要得",
      "lib_retrofit",
      "Retrofit",
      "",
    )
    assertThat(sheetContent).containsExactly(*expectedContent)
  }

  @OptIn(ExperimentalPathApi::class)
  private fun Path.copyToRecursively(target: Path) {
    copyToRecursively(target, followLinks = true, overwrite = true)
  }
}
