/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.repository

import org.chronolens.core.versioning.VcsProxyFactoryMock
import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.project
import org.chronolens.test.core.model.sourceFile
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
                        "@class": "SourceFile",
                        "entities": [{
                            "id": "src/Worksheet.mock#println()",
                            "@class": "Function"
                        }]
                }""")
                change("src/Test.mock" to """{
                        "id": "src/Test.mock",
                        "@class": "SourceFile"
                }""")
                change("src/BuildVersion.mock" to """
                        "id": "src/BuildVersion.mock",
                        "@class": "SourceFile"
                """)
                change("README.md" to "## Mock repository\n")
            }

            revision {
                change("src/Main.mock" to """{
                        "id": "src/Main.mock",
                        "@class": "SourceFile",
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

    @Test fun `test list revisions`() {
        val expected = listOf("0", "1")
        val actual = repository.listRevisions()
        assertEquals(expected, actual)
    }

    @Test fun `test get source`() {
        val expected = sourceFile("src/Main.mock").build {
            type("Main") {}
        }
        val actual = repository.getSource("src/Main.mock")
        assertEquals(expected, actual)
    }

    @Test fun `test get non-existing source returns null`() {
        val actual = repository.getSource("src/Test.mock")
        assertNull(actual)
    }

    @Test fun `test get non-parsable source returns null`() {
        val actual = repository.getSource("README.md")
        assertNull(actual)
    }

    @Test fun `test get invalid source returns latest valid version`() {
        val expected = sourceFile("src/Worksheet.mock").build {
            function("println()") {}
        }
        val actual = repository.getSource("src/Worksheet.mock")
        assertEquals(expected, actual)
    }

    @Test fun `test get invalid source with no history returns empty`() {
        val expected = sourceFile("src/Error.mock").build {}
        val actual = repository.getSource("src/Error.mock")
        assertEquals(expected, actual)
    }

    @Test fun `test get invalid path throws`() {
        assertFailsWith<IllegalArgumentException> {
            repository.getSource("src/../Main.mock")
        }
        assertFailsWith<IllegalArgumentException> {
            repository.getSource("src/./Main.mock")
        }
        assertFailsWith<IllegalArgumentException> {
            repository.getSource("src//Main.mock")
        }
    }

    @Test fun `test get snapshot`() {
        val expected = project {
            sourceFile("src/Main.mock") {
                type("Main") {}
            }
            sourceFile("src/BuildVersion.mock") {}
            sourceFile("src/Worksheet.mock") {
                function("println()") {}
            }
            sourceFile("src/Error.mock") {}
        }
        val actual = repository.getSnapshot()
        assertEquals(expected, actual)
    }

    @Test fun `test get history`() {
        val expected = project {
            sourceFile("src/Main.mock") {
                type("Main") {}
            }
            sourceFile("src/BuildVersion.mock") {}
            sourceFile("src/Worksheet.mock") {
                function("println()") {}
            }
            sourceFile("src/Error.mock") {}
        }
        val transactions = repository.getHistory()
        val actual = project {}
        transactions.map(Transaction::edits).forEach(actual::apply)
        assertEquals(expected, actual)
    }
}
