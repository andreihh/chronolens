# Metanalysis

[![](https://jitpack.io/v/andreihh/metanalysis.svg)](https://jitpack.io/#andreihh/metanalysis)
[![Build Status](https://travis-ci.org/andreihh/metanalysis.svg)](https://travis-ci.org/andreihh/metanalysis)
[![codecov](https://codecov.io/gh/andreihh/metanalysis/branch/master/graph/badge.svg)](https://codecov.io/gh/andreihh/metanalysis)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Features

- an abstract model to represent code metadata and transactions (differences
between two versions of code metadata)
- JSON serialization utilities for code metadata and transactions
- a Git proxy module
- a Java source file parser which extracts Java code metadata

## Using Metanalysis

### Environment requirements

In order to use `metanalysis` you need to have `JDK 1.7` or newer.

### Using the command line

Download the most recently released distribution from
[here](https://github.com/andreihh/metanalysis/releases) and run the executable
from the `bin` directory: `./metanalysis help`.

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

### Example Java usage

There are three ways to interact with a repository from Java:
```java
// Connect to the repository directly, without persisting anything. All method
// calls are delegated to a VCS proxy.
final Repository repository = InteractiveRepository.connect();
// ...

// Connect to the previously persisted data from the repository. All method
// calls read the persisted data from the disk.
final Repository repository = PersistentRepository.load();
// ...

// Connect to the repository directly, persist the data and then interact with
// the persisted data. The repository will be analyzed and persisted to disk.
final Project project = InteractiveProject.connect().persist();
// ...
```

There are three ways to process data from a repository:
```java
// Process all sources individually.
for (final String path : repository.listSources()) {
    final SourceUnit sourceUnit = repository.getSourceUnit(path);
    // ...
}

// Process the latest snapshot.
final Project project = repository.getSnapshot();
// ...

// Process the transaction history.
final Iterable<Transaction> history = repository.getHistory();
// ...
```

Processing a `SourceNode` can be achieved using the `Visitor` pattern:
```java
abstract class NodeVisitor {
    public abstract void visit(SourceUnit sourceUnit);
    public abstract void visit(Type type);
    public abstract void visit(Function function);
    public abstract void visit(Variable variable);
    public final void visit(SourceNode node) {
        // safe to use `instanceof` because the class hierarchy is sealed
        if (node instanceof SourceUnit) {
            visit((SourceUnit) node);
        } else if (node instanceof Type) {
            visit((Type) node);
        } else if (node instanceof Function) {
            visit((Function) node);
        } else if (node instanceof Variable) {
            visit((Variable) node);
        } else {
            // should never be executed
            throw new AssertionError("Unknown node type!");
        }
    }
}
```

Processing a `Transaction` can also be achieved using the `Visitor` pattern:
```java
abstract class TransactionVisitor {
    protected final Project project;
    
    public TransactionVisitor(Project project) {
        this.project = project;
    }

    protected abstract void visit(AddNode edit);
    protected abstract void visit(RemoveNode edit);
    protected abstract void visit(EditType edit);
    protected abstract void visit(EditFunction edit);
    protected abstract void visit(EditVariable edit);
    protected final void visit(ProjectEdit edit) {
        // safe to use `instanceof` because the class hierarchy is sealed
        if (edit instanceof AddNode) {
            visit((AddNode) edit);
        } else if (edit instanceof RemoveNode) {
            visit((RemoveNode) edit);
        } else if (edit instanceof EditType) {
            visit((EditType) edit);
        } else if (edit instanceof EditFunction) {
            visit((EditFunction) edit);
        } else if (edit instanceof EditVariable) {
            visit((EditVariable) edit);
        } else {
            // should never be executed
            throw new AssertionError("Unknown edit type!");
        }
    }

    public final void visit(Transaction transaction) {
        for (ProjectEdit edit : transaction.getEdits()) {
            visit(edit);
        }
    }
}
```

## Building

To build the project, run `./gradlew build`.

To build the distribution `zip` in the `build/distributions` directory, run
`./gradlew distZip`.

## Documentation

To generate the documentation, run `./gradlew dokka`.

## Licensing

The code is available under the Apache V2.0 License.
