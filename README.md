# Metanalysis

[![](https://jitpack.io/v/andreihh/metanalysis.svg)](https://jitpack.io/#andreihh/metanalysis)
[![Build Status](https://travis-ci.org/andreihh/metanalysis.svg)](https://travis-ci.org/andreihh/metanalysis)
[![codecov](https://codecov.io/gh/andreihh/metanalysis/branch/master/graph/badge.svg)](https://codecov.io/gh/andreihh/metanalysis)
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
[here](https://github.com/andreihh/metanalysis/releases) and run the executable
from the `bin` directory: `./metanalysis-cli help`.

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
    compile "com.github.andreihh.metanalysis:metanalysis-core:$version"
    testCompile "com.github.andreihh.metanalysis:metanalysis-test:$version"
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
    <groupId>com.github.andreihh.metanalysis</groupId>
    <artifactId>metanalysis-core</artifactId>
    <version>$version</version>
    <scope>compile</scope>
  </dependency>
  <dependency>
    <groupId>com.github.andreihh.metanalysis</groupId>
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

### Example Java usage

There are three ways to interact with a repository from Java.

1. Connect to the repository directly, without persisting anything:

```java
final Project project = InteractiveProject.connect();
// all project method calls are delegated to a VCS proxy
for (final String filePath : project.listFiles()) {
    final SourceFile model = project.getFileModel(filePath);
    final List<HistoryEntry> history = project.getFileHistory(filePath);
    // process the model and history
}
```

2. Connect to the previously persisted data from the repository:

```java
final Project project = PersistentProject.load();
// all project method calls read the persisted data from the disk
for (final String filePath : project.listFiles()) {
    final SourceFile model = project.getFileModel(filePath);
    final List<HistoryEntry> history = project.getFileHistory(filePath);
    // process the model and history
}
```

3. Connect to the repository directly, persist the data and then interact with
the persisted data:

```java
// the repository will be analyzed and persisted to disk
final Project project = InteractiveProject.connect().persist();
// all project method calls read the persisted data from the disk
for (final String filePath : project.listFiles()) {
    final SourceFile model = project.getFileModel(filePath);
    final List<HistoryEntry> history = project.getFileHistory(filePath);
    // process the model and history
}
```

Processing a `SourceFile` or `Node` can be achieved using the `Visitor` pattern:

```java
abstract class CodeVisitor {
    public abstract void visit(SourceFile sourceFile);
    public abstract void visit(Node.Type type);
    public abstract void visit(Node.Variable variable);
    public abstract void visit(Node.Function function);
    
    public final void visit(Node node) {
        // safe to use `instanceof` because the class hierarchy is sealed
        if (node instanceof Node.Type) {
            visit((Node.Type) node);
        } else if (node instanceof Node.Variable) {
            visit((Node.Variable) node);
        } else if (node instanceof Node.Function) {
            visit((Node.Function) node);
        }
    }
}
```

## Building

To build the project, run `./gradlew build`.

To build the distribution `zip` in the `metanalysis-cli/build/distributions`
directory, run `./gradlew distZip`.

## Documentation

To generate the documentation, run `./gradlew dokka`.

## Licensing

The code is available under the Apache V2.0 License.
