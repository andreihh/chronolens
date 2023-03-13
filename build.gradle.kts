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
    // Direct and transitive project dependencies are selected for aggregation. The
    // ":api:chronolens-cli" application project should assemble all other subprojects
    // (":chronolens-model", ":chronolens-api", ":chronolens-core", and all ":plugins" subprojects).
    testReportAggregation(project(":app:chronolens-cli"))
    jacocoAggregation(project(":app:chronolens-cli"))
}

reporting {
    reports {
        val aggregateTestReport by creating(AggregateTestReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }

        val codeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

