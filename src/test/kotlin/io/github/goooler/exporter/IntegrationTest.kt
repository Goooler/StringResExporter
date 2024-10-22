package io.github.goooler.exporter

import app.cash.burst.Burst
import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyToRecursively
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readLines
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@Burst
class IntegrationTest(
  private val testCase: TestCase,
) {
  @TempDir
  private lateinit var tempDir: Path

  @Test
  fun exportAndImport() {
    val importedRes = tempDir.resolve("resInput")
    requireResourceAsPath("/res").copyToRecursively(importedRes)
    requireResourceAsPath("/res").copyToRecursively(importedRes)
    convert(
      testCase.useCli,
      "--res2xls",
      importedRes.absolutePathString(),
      tempDir.absolutePathString(),
    )

    val exportedXls = tempDir.resolve("output.xls")
    assertThat(exportedXls.exists()).isTrue()
    assertThat(exportedXls.isRegularFile()).isTrue()
    validateXlsContent(exportedXls)

    val exportedRes = tempDir.resolve("resOutput")
    convert(
      testCase.useCli,
      "--xls2res",
      exportedXls.absolutePathString(),
      exportedRes.absolutePathString(),
    )

    assertThat(exportedRes.exists()).isTrue()
    assertThat(exportedRes.listDirectoryEntries().size).isEqualTo(3)
    validateResContent(importedRes, exportedRes)
  }

  private fun validateXlsContent(exportedXls: Path) {
    val workbook = WorkbookFactory.create(exportedXls.inputStream())

    StringRes.TAG.let { tag ->
      assertThat(workbook.getSheet(tag).stringValues).containsExactly(*readCsvValues(tag))
    }
    PluralsRes.TAG.let { tag ->
      assertThat(workbook.getSheet(tag).stringValues).containsExactly(*readCsvValues(tag))
    }
    ArrayRes.TAG.let { tag ->
      assertThat(workbook.getSheet(tag).stringValues).containsExactly(*readCsvValues(tag))
    }
  }

  private fun validateResContent(importedRes: Path, exportedRes: Path) {
    validateStringResContent(importedRes, exportedRes)
    validatePluralsResContent(importedRes, exportedRes)
    validateArrayResContent(importedRes, exportedRes)
  }

  private fun validateStringResContent(importedRes: Path, exportedRes: Path) {
    val expected = parseRes<StringRes>(importedRes, "strings.xml")
    val actual = parseRes<StringRes>(exportedRes, "strings.xml").toTypedArray()

    assertThat(expected.isNotEmpty()).isEqualTo(true)
    assertThat(actual.isNotEmpty()).isEqualTo(true)
    assertThat(expected).containsAtLeast(*actual)
  }

  private fun validatePluralsResContent(importedRes: Path, exportedRes: Path) {
    val expected = parseRes<PluralsRes>(importedRes, "strings.xml")
    val actual = parseRes<PluralsRes>(exportedRes, "plurals.xml").toTypedArray()

    assertThat(expected.isNotEmpty()).isEqualTo(true)
    assertThat(actual.isNotEmpty()).isEqualTo(true)
    assertThat(expected).containsExactly(*actual)
  }

  private fun validateArrayResContent(importedRes: Path, exportedRes: Path) {
    val expected = parseRes<ArrayRes>(importedRes, "strings.xml")
    val actual = parseRes<ArrayRes>(exportedRes, "arrays.xml")

    assertThat(expected.isNotEmpty()).isEqualTo(true)
    assertThat(actual.isNotEmpty()).isEqualTo(true)
    assertThat(expected.map(ArrayRes::name))
      .containsExactly(*actual.map(ArrayRes::name).toTypedArray())
    assertThat(expected.flatMap(ArrayRes::values))
      .containsAtLeast(*actual.flatMap(ArrayRes::values).toTypedArray())
  }

  private inline fun <reified T : TranslatableRes> parseRes(resRoot: Path, resFile: String): List<T> {
    return parseResFiles(resRoot.absolutePathString(), resFile)
      .flatMap { subFolder ->
        SAXBuilder().build(subFolder.inputStream()).rootElement.children.asSequence()
          .map(Element::toTransResOrNull)
          .filterIsInstance<T>()
      }
      .toList()
  }

  private fun convert(useCli: Boolean, converter: String, inputPath: String, outputPath: String) {
    if (useCli) {
      CommandLineTestRunner(converter, inputPath, outputPath).run()
    } else {
      main(converter, inputPath, outputPath)
    }
  }

  private fun readCsvValues(name: String): Array<String> {
    return requireResourceAsPath("/sheets/$name.csv").readLines().asSequence()
      .flatMap { it.split(",").asSequence() }.toList().toTypedArray()
  }

  private val Sheet.stringValues: List<String>
    get() = rowIterator().asSequence()
      .flatMap { it.cellIterator().asSequence() }
      .map { it.stringValue }.toList()

  @OptIn(ExperimentalPathApi::class)
  private fun Path.copyToRecursively(target: Path) {
    copyToRecursively(target, followLinks = true, overwrite = true)
  }

  enum class TestCase(val useCli: Boolean) {
    Cli(true),
    Main(false),
  }
}
