/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.interactive

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.chronolens.core.analysis.InvalidOptionException
import org.chronolens.core.model.qualifiedSourcePathOf
import org.chronolens.core.model.type
import org.chronolens.core.repository.CorruptedRepositoryException
import org.chronolens.test.core.analysis.OptionsProviderBuilder
import org.chronolens.test.core.model.add
import org.chronolens.test.core.model.edit
import org.chronolens.test.core.model.revision
import org.chronolens.test.core.model.type
import org.chronolens.test.core.model.variable
import org.chronolens.test.core.repository.repository
import org.chronolens.test.core.repository.revisionListOf

class ModelTest {
    private fun create(qualifiedId: String, revision: String? = null): ModelAnalyzer {
        val options = OptionsProviderBuilder()
        options.setOption("qualified-id", qualifiedId)
        if (revision != null) {
            options.setOption("rev", revision)
        }
        return ModelSpec().create(options.build())
    }

    @Test
    fun analyze_whenInvalidQualifiedId_throws() {
        val analyzer = create(qualifiedId = "src///Main.java")
        val repository = repository { +revision("1") {} }

        assertFailsWith<InvalidOptionException> { analyzer.analyze(repository) }
    }

    @Test
    fun analyze_whenInvalidRevision_throws() {
        val analyzer = create(qualifiedId = "src/Main.java", revision = "invalid-rev-(!*&$(#Q")
        val repository = repository { +revision("1") {} }

        assertFailsWith<InvalidOptionException> { analyzer.analyze(repository) }
    }

    @Test
    fun analyze_whenMissingSourceFile_throws() {
        val analyzer = create(qualifiedId = "src/Test.java:Test", revision = "1")
        val repository = repository {
            +revision("1") { +qualifiedSourcePathOf("src/Main.java").add {} }
        }

        assertFailsWith<InvalidOptionException> { analyzer.analyze(repository) }
    }

    @Test
    fun analyze_whenMissingSourceNode_throws() {
        val analyzer = create(qualifiedId = "src/Main.java:Test", revision = "1")
        val repository = repository {
            +revision("1") {
                +qualifiedSourcePathOf("src/Main.java").add {
                    +type("Main") { supertypes("Object") }
                }
            }
        }

        assertFailsWith<InvalidOptionException> { analyzer.analyze(repository) }
    }

    @Test
    fun analyze_whenCorruptedRepository_throws() {
        val analyzer = create(qualifiedId = "src/Main.java", revision = "HEAD")
        val repository = repository {}

        assertFailsWith<CorruptedRepositoryException> { analyzer.analyze(repository) }
    }

    @Test
    fun analyze_returnsSourceNodeAtRevision() {
        val repository = repository {
            +revision("1") { +qualifiedSourcePathOf("src/Main.java").add { +type("Main") {} } }
            +revision("2") {
                +qualifiedSourcePathOf("src/Main.java").type("Main").edit {
                    supertypes { +"Object" }
                }
            }
            +revision("3") {
                +qualifiedSourcePathOf("src/Main.java").type("Main").edit {
                    supertypes { -"Object" }
                }
            }
        }

        val expectedSourceNodes =
            revisionListOf("1", "2", "3")
                .zip(
                    listOf(
                        type("Main") {},
                        type("Main") { supertypes("Object") },
                        type("Main") {},
                    )
                )

        assertEquals(
            expected = expectedSourceNodes.last().second,
            actual = create(qualifiedId = "src/Main.java:Main").analyze(repository).sourceNode
        )

        for ((revisionId, expected) in expectedSourceNodes) {
            val actual =
                create(qualifiedId = "src/Main.java:Main", revision = revisionId.toString())
                    .analyze(repository)
                    .sourceNode

            assertEquals(expected, actual)
        }
    }

    @Test
    fun reportToString_prettyPrintsSourceNode() {
        val expected =
            """
            type Main
            `- members:
               `- variable VERSION

            """
                .trimIndent()

        val actual = ModelReport(type("Main") { +variable("VERSION") {} }).toString()

        assertEquals(expected, actual)
    }
}
