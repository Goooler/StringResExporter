# StringResExporter

StringResExporter is a utility designed to facilitate the export and import of string resources between Android projects and XLS files.

Usage
-----

```sh
# Export string resources from an Android project to a XLS file.
string-res-exporter --res2xls /path/to/res /path/to/xls

# Import string resources from a XLS file to an Android project.
string-res-exporter --xls2res /path/to/output.xls /path/to/res
```

In the usages below, [`./src/test/resources/res`](src/test/resources/res) will be exported to `/path/to/xls/output.xls`,
which contains three sheets: [`StringRes`](src/test/resources/sheets/StringRes.csv),
[`PluralsRes`](src/test/resources/sheets/PluralsRes.csv) and [`ArrayRes`](src/test/resources/sheets/ArrayRes.csv).
If you want to import resources from a XLS file, you must use the same formatted sheets,
including the same sheet names and column names

## Install

**Mac OS** or **Linux**

```sh
brew install Goooler/repo/string-res-exporter
```

**Other**

Download standalone JAR from
[latest release](https://github.com/Goooler/StringResExporter/releases/latest).
On MacOS and Linux you can `chmod +x` and execute the `.jar` directly.
On Windows use `java -jar`.
