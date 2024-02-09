package io.github.goooler.exporter

import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyToRecursively
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class IntegrationTest {

  @ParameterizedTest
  @ValueSource(booleans = [false, true])
  fun exportAndImport(useCli: Boolean) {
    val importedRes = tempDir.resolve("resInput")
    Paths.get(requireResource("/res").toURI()).copyToRecursively(importedRes)
    convert(
      useCli,
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
      useCli,
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
    validateStringResSheet(workbook.getSheet(StringRes.TAG))
    validatePluralsResSheet(workbook.getSheet(PluralsRes.TAG))
    validateArrayResSheet(workbook.getSheet(ArrayRes.TAG))
  }

  private fun validateStringResSheet(sheet: Sheet) {
    val expected = arrayOf(
      "key", "values", "values-it", "values-zh-rCN",
      "first", "first", "primo", "",
      "second", "second", "", "第二",
      "third", "third", "", "",
      "forth", "forth", "quarto", "",
      "fifth", "fifth", "", "第五",
    )
    assertThat(sheet.stringValues).containsExactly(*expected)
  }

  private fun validatePluralsResSheet(sheet: Sheet) {
    val expected = arrayOf(
      "key", "quantity", "values", "values-it", "values-zh-rCN",
      "apples", "zero", "", "", "",
      "", "one", "apple", "mela", "",
      "", "two", "", "", "",
      "", "few", "", "", "",
      "", "many", "", "mele", "",
      "", "other", "apples", "mele", "",
      "bananas", "zero", "", "", "",
      "", "one", "banana", "", "",
      "", "two", "", "", "",
      "", "few", "", "", "",
      "", "many", "", "", "",
      "", "other", "bananas", "", "香蕉",
    )
    assertThat(sheet.stringValues).containsExactly(*expected)
  }

  private fun validateArrayResSheet(sheet: Sheet) {
    val expected = arrayOf(
      "key", "values", "values-it", "values-zh-rCN",
      "colors", "red", "rosso", "",
      "", "green", "verde", "",
      "", "blue", "blu", "",
      "animals", "cat", "", "猫",
      "", "dog", "", "狗",
      "", "bird", "", "",
      "", "fish", "", "",
    )
    assertThat(sheet.stringValues).containsExactly(*expected)
  }

  private fun validateResContent(importedRes: Path, exportedRes: Path) {
    validateStringResContent(importedRes, exportedRes)
    validatePluralsResContent(importedRes, exportedRes)
    validateArrayResContent(importedRes, exportedRes)
  }

  private fun validateStringResContent(importedRes: Path, exportedRes: Path) {
    fun Sequence<TranslatableRes>.convert() = asSequence()
      .filterIsInstance<StringRes>()
      .filter {
        it.value.isNotEmpty()
      }.toList()

    val expected = parseRes(importedRes, "strings.xml").convert()
    val actual = parseRes(exportedRes, "strings.xml").convert().toTypedArray()

    assertThat(expected.isNotEmpty()).isEqualTo(true)
    assertThat(actual.isNotEmpty()).isEqualTo(true)
    assertThat(expected).containsAtLeast(*actual)
  }

  private fun validatePluralsResContent(importedRes: Path, exportedRes: Path) {
    fun Sequence<TranslatableRes>.convert() = filterIsInstance<PluralsRes>().toList()

    val expected = parseRes(importedRes, "strings.xml").convert()
    val actual = parseRes(exportedRes, "plurals.xml").convert().toTypedArray()

    assertThat(expected.isNotEmpty()).isEqualTo(true)
    assertThat(actual.isNotEmpty()).isEqualTo(true)
    assertThat(expected).containsExactly(*actual)
  }

  private fun validateArrayResContent(importedRes: Path, exportedRes: Path) {
    fun Sequence<TranslatableRes>.convert() = filterIsInstance<ArrayRes>().toList()

    val expected = parseRes(importedRes, "strings.xml").convert()
    val actual = parseRes(exportedRes, "arrays.xml").convert()

    assertThat(expected.isNotEmpty()).isEqualTo(true)
    assertThat(actual.isNotEmpty()).isEqualTo(true)
    assertThat(expected.map(ArrayRes::name))
      .containsExactly(*actual.map(ArrayRes::name).toTypedArray())
    assertThat(expected.flatMap(ArrayRes::values))
      .containsAtLeast(*actual.flatMap(ArrayRes::values).toTypedArray())
  }

  private fun parseRes(resRoot: Path, resFile: String): Sequence<TranslatableRes> {
    return parseResFiles(resRoot.absolutePathString(), resFile)
      .flatMap { subFolder ->
        SAXBuilder().build(subFolder.inputStream()).rootElement.children.asSequence()
          .map(Element::toTransResOrNull)
          .filterNotNull()
      }
  }

  private fun convert(useCli: Boolean, converter: String, inputPath: String, outputPath: String) {
    if (useCli) {
      CommandLineTestRunner(converter, inputPath, outputPath).run()
    } else {
      main(converter, inputPath, outputPath)
    }
  }

  private val Sheet.stringValues: List<String>
    get() = rowIterator().asSequence()
      .flatMap { it.cellIterator().asSequence() }
      .map { it.stringValue }.toList()

  @OptIn(ExperimentalPathApi::class)
  private fun Path.copyToRecursively(target: Path) {
    copyToRecursively(target, followLinks = true, overwrite = true)
  }

  companion object {
    // Workaround for https://github.com/junit-team/junit5/issues/2811.
    @JvmStatic
    private lateinit var tempDir: Path

    @JvmStatic
    @BeforeAll
    fun before() {
      tempDir = Files.createTempDirectory(null)
    }

    @JvmStatic
    @AfterAll
    fun after() {
      tempDir.toFile().delete()
    }
  }
}
