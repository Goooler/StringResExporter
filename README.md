# StringResExporter

StringResExporter is a tool for exporting and importing string resources from an Android project to a XLS file.

Usage
-----

```shell
# Export string resources from an Android project to a XLS file.
./StringResExporter-0.1.0-SNAPSHOT-binary.jar --res2xls /path/to/res /path/to/xls

# Import string resources from a XLS file to an Android project.
./StringResExporter-0.1.0-SNAPSHOT-binary.jar --xls2res /path/to/output.xls /path/to/res
```

## Install

**Mac OS** or **Linux**

```shell
brew install Goooler/repo/string-res-exporter
```

**Other**

Download standalone JAR from
[latest release](https://github.com/Goooler/StringResExporter/releases/latest).
On MacOS and Linux you can `chmod +x` and execute the `.jar` directly.
On Windows use `java -jar`.
