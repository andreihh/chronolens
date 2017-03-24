## Metanalysis

[![](https://jitpack.io/v/andrei-heidelbacher/metanalysis.svg)](https://jitpack.io/#andrei-heidelbacher/metanalysis)
[![Build Status](https://travis-ci.org/andrei-heidelbacher/metanalysis.png)](https://travis-ci.org/andrei-heidelbacher/metanalysis)
[![codecov](https://codecov.io/gh/andrei-heidelbacher/metanalysis/branch/master/graph/badge.svg)](https://codecov.io/gh/andrei-heidelbacher/metanalysis)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

### Features

- an abstract model which contains code metadata
- an abstract transaction model to represent diffs between code metadata models
- JSON serialization utilities for code metadata and diffs
- a Java source file parser which extracts Java code metadata

### Using Metanalysis

Example `java` usage:

```java
class PrinterVisitor extends NodeVisitor {
    @Override
    void visit(Node.Type type) {
        System.out.println(
                "Type(" + type.getName() + ") : " + type.getSupertypes());
        System.out.println("{\n");
    }

    @Override
    void endVisit(Node.Type type) {
        System.out.println("\n}");
    }

    @Override
    void visit(Node.Variable variable) {
        String init = variable.initializer != null
                ? (" = " + variable.initializer)
                : "";
        System.out.println("Variable(" + variable.getName() + ")" + init);
    }

    @Override
    void visit(Node.Function function) {
        String body = function.body != null ? function.body : "";
        println("Function(" + function.signature + ")" + body);
    }
}
```

### Documentation

To generate the documentation, run ```./gradlew javadocJar```.

### Licensing

The code is available under the Apache V2.0 License.
