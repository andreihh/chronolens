/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metanalysis.core.repository

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.project
import org.metanalysis.test.core.model.sourceUnit
import org.metanalysis.core.versioning.VcsProxyFactoryMock
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Ignore
abstract class RepositoryTest {
    protected val repository: Repository by lazy { createRepository() }

    protected abstract fun createRepository(): Repository

    @Before fun initVcsRepository() {
        VcsProxyFactoryMock.setRepository {
            revision {
                change("src/Main.mock" to "{")
                change("src/Worksheet.mock" to """{
                        "id": "src/Worksheet.mock",
                        "@class": "SourceUnit",
                        "entities": [{
                            "id": "src/Worksheet.mock:println()",
                            "@class": "Function"
                        }]
                }""")
                change("src/Test.mock" to """{
                        "id": "src/Test.mock",
                        "@class": "SourceUnit"
                }""")
                change("src/BuildVersion.mock" to """
                        "id": "src/BuildVersion.mock",
                        "@class": "SourceUnit"
                """)
                change("README.md" to "## Mock repository\n")
            }

            revision {
                change("src/Main.mock" to """{
                        "id": "src/Main.mock",
                        "@class": "SourceUnit",
                        "entities": [{
                            "id": "src/Main.mock:Main",
                            "@class": "Type"
                        }]
                }""")
                change("src/Test.mock" to null)
                change("src/Error.mock" to "{")
                change("src/Worksheet.mock" to "{")
                change("README.md" to "## Mock repository v2\n")
            }
        }
    }

    @After fun resetVcsRepository() {
        VcsProxyFactoryMock.resetRepository()
    }

    @Test fun `test get head id`() {
        val expected = "1"
        val actual = repository.getHeadId()
        assertEquals(expected, actual)
    }

    @Test fun `test list sources`() {
        val expected = setOf(
                "src/Main.mock",
                "src/BuildVersion.mock",
                "src/Error.mock",
                "src/Worksheet.mock"
        )
        val actual = repository.listSources()
        assertEquals(expected, actual)
    }

    @Test fun `test get source unit`() {
        val expected = sourceUnit("src/Main.mock") {
            type("Main") {}
        }
        val actual = repository.getSourceUnit("src/Main.mock")
        assertEquals(expected, actual)
    }

    @Test fun `test get non-existing source unit returns null`() {
        val actual = repository.getSourceUnit("src/Test.mock")
        assertNull(actual)
    }

    @Test fun `test get non-parsable source unit returns null`() {
        val actual = repository.getSourceUnit("README.md")
        assertNull(actual)
    }

    @Test fun `test get invalid source unit returns latest valid version`() {
        val expected = sourceUnit("src/Worksheet.mock") {
            function("println()") {}
        }
        val actual = repository.getSourceUnit("src/Worksheet.mock")
        assertEquals(expected, actual)
    }

    @Test fun `test get invalid source unit with no history returns empty`() {
        val expected = sourceUnit("src/Error.mock") {}
        val actual = repository.getSourceUnit("src/Error.mock")
        assertEquals(expected, actual)
    }

    @Test fun `test get snapshot`() {
        val expected = project {
            sourceUnit("src/Main.mock") {
                type("Main") {}
            }
            sourceUnit("src/BuildVersion.mock") {}
            sourceUnit("src/Worksheet.mock") {
                function("println()") {}
            }
            sourceUnit("src/Error.mock") {}
        }
        val actual = repository.getSnapshot()
        assertEquals(expected, actual)
    }

    @Test fun `test get history`() {
        val expected = project {
            sourceUnit("src/Main.mock") {
                type("Main") {}
            }
            sourceUnit("src/BuildVersion.mock") {}
            sourceUnit("src/Worksheet.mock") {
                function("println()") {}
            }
            sourceUnit("src/Error.mock") {}
        }
        val transactions = repository.getHistory()
        val actual = project {}
        transactions.map(Transaction::edits).forEach(actual::apply)
        assertEquals(expected, actual)
    }
}
