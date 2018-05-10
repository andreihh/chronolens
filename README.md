# ChronoLens

[![](https://jitpack.io/v/andreihh/chronolens.svg)](https://jitpack.io/#andreihh/chronolens)
[![Build Status](https://travis-ci.org/andreihh/chronolens.svg)](https://travis-ci.org/andreihh/chronolens)
[![codecov](https://codecov.io/gh/andreihh/chronolens/branch/master/graph/badge.svg)](https://codecov.io/gh/andreihh/chronolens)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Features

### History analysis and extraction

- an abstract model to represent the history of a system (sources and
differences between two versions of the same source)
- JSON serialization utilities for the defined model
- a Git proxy module
- a Java parser that extracts the source model from Java files
- utilities for building command-line interfaces

### Anti-pattern detection

- an analyzer that detects decapsulations of fields
- an analyzer that detects hotspots of a system
- an analyzer that detects instances of the *Divergent Change* code smell

## Using ChronoLens

### Environment requirements

In order to use `ChronoLens` you need to have `JDK 1.8` or newer.

### Using the command line

Download the most recently released distribution from
[here](https://github.com/andreihh/chronolens/releases) and run the executable
from the `bin` directory: `./chronolens help`.

The following is an example session of commands that inspect a remote `git`
repository:
```
# Clones the repository to the current working directory.
git clone $URL .

# Prints help and lists the available commands.
chronolens help

# Prints usage information for the 'model' command.
chronolens help model

# Prints the source model for the specified file as it is found in the 'HEAD'
# revision.
chronolens model --id $PATH

# Persists the history model.
chronolens persist

# Run the 'decapsulations' analysis that makes use of the persisted model.
chronolens decapsulations | less

# Deletes the persisted model.
chronolens clean
```

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
    compile "com.github.andreihh.chronolens:chronolens-core:$version"
    testCompile "com.github.andreihh.chronolens:chronolens-test:$version"
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
    <groupId>com.github.andreihh.chronolens</groupId>
    <artifactId>chronolens-core</artifactId>
    <version>$version</version>
    <scope>compile</scope>
  </dependency>
  <dependency>
    <groupId>com.github.andreihh.chronolens</groupId>
    <artifactId>chronolens-test</artifactId>
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
    final SourceFile source = repository.getSource(path);
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
    public abstract void visit(SourceFile sourceFile);
    public abstract void visit(Type type);
    public abstract void visit(Function function);
    public abstract void visit(Variable variable);
    public final void visit(SourceNode node) {
        // safe to use `instanceof` because the class hierarchy is sealed
        if (node instanceof SourceFile) {
            visit((SourceFile) node);
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
    /** The current snapshot of the repository. */
    protected final Project project = new Project();
    
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
            visit(edit); // process this edit
            project.apply(edit); // update the snapshot with this edit
        }
    }
}
```

Implementing a command-line interface for the main executable can be done as
follows:
```java
import org.chronolens.core.cli.MainCommand;
import org.chronolens.core.cli.OptionDelegate;
import org.chronolens.core.cli.Utils;

public final class Main extends MainCommand {
    @Override
    public String getName() { return "echo-message"; }

    @Override
    protected String getVersion() { return "1.0"; }

    @Override
    protected String getHelp() { return "Prints a given message."; }

    private final OptionDelegate<String> message = option(String.class)
            .help("The message to display to standard output.")
            .paramLabel("TEXT")
            .defaultValue("Hello, world!")
            .provideDelegate(this, "message");

    private String getMessage() { return message.getValue(); }

    @Override
    protected void execute() {
        System.out.println(getMessage());
    }

    public static void main(String[] args) {
        Utils.run(new Main(), args);
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
