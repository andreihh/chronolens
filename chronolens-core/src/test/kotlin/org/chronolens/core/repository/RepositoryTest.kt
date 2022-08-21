/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTreeEdit.Companion.apply
import org.chronolens.core.versioning.VcsProxyFactoryMock
import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.function
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.model.sourceTree
import org.chronolens.test.core.model.type
import org.junit.After
import org.junit.Before
import org.junit.Test

abstract class RepositoryTest {
    protected val repository: Repository by lazy { createRepository() }

    protected abstract fun createRepository(): Repository

    @Before
    fun initVcsRepository() {
        VcsProxyFactoryMock.setRepository {
            revision {
                change("src/Main.fake" to "{")
                change(
                    "src/Worksheet.fake" to
                        """{
                        "path": "src/Worksheet.fake",
                        "@class": "SourceFile",
                        "entities": [{
                            "signature": "println()",
                            "@class": "Function"
                        }]
                    }"""
                )
                change(
                    "src/Test.fake" to
                        """{
                        "path": "src/Test.fake",
                        "@class": "SourceFile"
                    }"""
                )
                change(
                    "src/BuildVersion.fake" to
                        """
                        "path": "src/BuildVersion.fake",
                        "@class": "SourceFile"
                    """
                )
                change("README.md" to "## Fake repository\n")
            }

            revision {
                change(
                    "src/Main.fake" to
                        """{
                        "path": "src/Main.fake",
                        "@class": "SourceFile",
                        "entities": [{
                            "name": "Main",
                            "@class": "Type"
                        }]
                    }"""
                )
                change("src/Test.fake" to null)
                change("src/Error.fake" to "{")
                change("src/Worksheet.fake" to "{")
                change("README.md" to "## Fake repository v2\n")
            }
        }
    }

    @After
    fun resetVcsRepository() {
        VcsProxyFactoryMock.resetRepository()
    }

    @Test
    fun `test get head id`() {
        val expected = RevisionId("1")
        val actual = repository.getHeadId()
        assertEquals(expected, actual)
    }

    @Test
    fun `test list sources`() {
        val expected =
            setOf(
                SourcePath("src/Main.fake"),
                SourcePath("src/BuildVersion.fake"),
                SourcePath("src/Error.fake"),
                SourcePath("src/Worksheet.fake")
            )
        val actual = repository.listSources()
        assertEquals(expected, actual)
    }

    @Test
    fun `test list revisions`() {
        val expected = listOf(RevisionId("0"), RevisionId("1"))
        val actual = repository.listRevisions()
        assertEquals(expected, actual)
    }

    @Test
    fun `test get source`() {
        val expected = sourceFile("src/Main.fake") { +type("Main") {} }
        val actual = repository.getSource(SourcePath("src/Main.fake"))
        assertEquals(expected, actual)
    }

    @Test
    fun `test get non-existing source returns null`() {
        val actual = repository.getSource(SourcePath("src/Test.fake"))
        assertNull(actual)
    }

    @Test
    fun `test get non-parsable source returns null`() {
        val actual = repository.getSource(SourcePath("README.md"))
        assertNull(actual)
    }

    @Test
    fun `test get invalid source returns latest valid version`() {
        val expected = sourceFile("src/Worksheet.fake") { +function("println()") {} }
        val actual = repository.getSource(SourcePath("src/Worksheet.fake"))
        assertEquals(expected, actual)
    }

    @Test
    fun `test get invalid source with no history returns empty`() {
        val expected = sourceFile("src/Error.fake") {}
        val actual = repository.getSource(SourcePath("src/Error.fake"))
        assertEquals(expected, actual)
    }

    @Test
    fun `test get snapshot`() {
        val expected = sourceTree {
            +sourceFile("src/Main.fake") { +type("Main") {} }
            +sourceFile("src/BuildVersion.fake") {}
            +sourceFile("src/Worksheet.fake") { +function("println()") {} }
            +sourceFile("src/Error.fake") {}
        }
        val actual = repository.getSnapshot()
        assertEquals(expected, actual)
    }

    @Test
    fun `test get history`() {
        val expected = sourceTree {
            +sourceFile("src/Main.fake") { +type("Main") {} }
            +sourceFile("src/BuildVersion.fake") {}
            +sourceFile("src/Worksheet.fake") { +function("println()") {} }
            +sourceFile("src/Error.fake") {}
        }
        val revisions = repository.getHistory()
        val actual = sourceTree {}
        actual.apply(revisions.flatMap(Revision::edits))
        assertEquals(expected, actual)
    }
}
