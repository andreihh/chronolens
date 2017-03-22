## Chronos

### Features

- an abstract model which contains code metadata
- an abstract transaction model to represent diffs between code metadata models
- JSON serialization utilities for code metadata and diffs
- a Java source file parser which extracts Java code metadata

### Using Chronos

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

To generate the documentation, run ```./gradlew dokkaJavadoc```.
