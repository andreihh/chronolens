plugins {
    base
    id("test-report-aggregation")
    id("jacoco-report-aggregation")
}

repositories {
    mavenCentral()
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

dependencies {
    // Direct and transitive project dependencies are selected for aggregation.
    // The ":chronolens-cli" application project should assemble all other
    // subprojects (":chronolens-core", all ":services" subprojects, and all
    // ":analyzers" subprojects).
    testReportAggregation(project(":chronolens-cli"))
    jacocoAggregation(project(":chronolens-cli"))
}

reporting {
    reports {
        val testAggregateTestReport by creating(AggregateTestReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }

        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

