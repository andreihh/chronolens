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

#### Environment requirements

In order to use `metanalysis` you need to have `JDK 1.8` or newer.

#### Using the command line

Download the most recently released `cli` artifact from
[here](https://github.com/andrei-heidelbacher/metanalysis/releases) and run it:

```java -jar metanalysis-cli-$version-all file_v1 file_v2 diff_output_file```

#### Using Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compile "com.github.andrei-heidelbacher.metanalysis:metanalysis-core:$version"
    runtime "com.github.andrei-heidelbacher.metanalysis:metanalysis-git:$version"
    runtime "com.github.andrei-heidelbacher.metanalysis:metanalysis-java:$version"
    testCompile "com.github.andrei-heidelbacher.metanalysis:metanalysis-test:$version"
}
```

Additionally, you must provide the service configuration file
`META-INF/services/org.metanalysis.core.model.Parser`.

### Documentation

To generate the documentation, run ```./gradlew javadocJar```.

### Licensing

The code is available under the Apache V2.0 License.
