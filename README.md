# Metanalysis

[![](https://jitpack.io/v/andrei-heidelbacher/metanalysis.svg)](https://jitpack.io/#andrei-heidelbacher/metanalysis)
[![Build Status](https://travis-ci.org/andrei-heidelbacher/metanalysis.svg)](https://travis-ci.org/andrei-heidelbacher/metanalysis)
[![codecov](https://codecov.io/gh/andrei-heidelbacher/metanalysis/branch/master/graph/badge.svg)](https://codecov.io/gh/andrei-heidelbacher/metanalysis)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Features

- an abstract model which contains code metadata
- an abstract transaction model to represent differences between code metadata
models
- JSON serialization utilities for code metadata and differences
- a Git integration module
- a Java source file parser which extracts Java code metadata

## Using Metanalysis

### Environment requirements

In order to use `metanalysis` you need to have `JDK 1.7` or newer.

### Using the command line

Download the most recently released distribution from
[here](https://github.com/andrei-heidelbacher/metanalysis/releases) and run the
executable from the `bin` directory: `./metanalysis-cli help`.

### Using Gradle

Add the `JitPack` repository to your build file:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Add the dependencies:
```groovy
dependencies {
    compile "com.github.andrei-heidelbacher.metanalysis:metanalysis-core:$version"
    testCompile "com.github.andrei-heidelbacher.metanalysis:metanalysis-test:$version"
}
```

### Using Maven

Add the `JitPack` repository to your build file:
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

Add the dependencies:
```xml
<dependencies>
  <dependency>
    <groupId>com.github.andrei-heidelbacher.metanalysis</groupId>
    <artifactId>metanalysis-core</artifactId>
    <version>$version</version>
    <scope>compile</scope>
  </dependency>
  <dependency>
    <groupId>com.github.andrei-heidelbacher.metanalysis</groupId>
    <artifactId>metanalysis-test</artifactId>
    <version>$version</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

### Configuration

If using the parsing and versioning modules as dependencies, you must provide
the following service configuration files:
- `META-INF/services/org.metanalysis.core.model.Parser` (for parsing)
- `META-INF/services/org.metanalysis.core.version.VcsProxy` (for versioning)

## Building

To build the project, run `./gradlew build`.

To build the distribution `zip` in the `metanalysis-cli/build/distributions`
directory, run `./gradlew distZip`.

## Documentation

To generate the documentation, run `./gradlew dokka`.

## Licensing

The code is available under the Apache V2.0 License.
