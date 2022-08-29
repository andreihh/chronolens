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
import org.chronolens.core.repository.CorruptedRepositoryException
import org.chronolens.test.core.analysis.OptionsProviderBuilder
import org.chronolens.test.core.model.add
import org.chronolens.test.core.model.function
import org.chronolens.test.core.model.remove
import org.chronolens.test.core.model.revision
import org.chronolens.test.core.model.type
import org.chronolens.test.core.repository.repository
import org.chronolens.test.core.repository.revisionListOf
import org.chronolens.test.core.repository.sourceSetOf

class LsTreeTest {
    private fun create(revision: String? = null): LsTreeAnalyzer {
        val options = OptionsProviderBuilder()
        if (revision != null) {
            options.setOption("rev", revision)
        }
        return LsTreeSpec().create(options.build())
    }

    @Test
    fun analyze_whenInvalidRevision_throws() {
        val analyzer = create("invalid-rev-(!*&$(#Q")
        val repository = repository { +revision("1") {} }

        assertFailsWith<InvalidOptionException> { analyzer.analyze(repository) }
    }

    @Test
    fun analyze_whenCorruptedRepository_throws() {
        val analyzer = create("HEAD")
        val repository = repository {}

        assertFailsWith<CorruptedRepositoryException> { analyzer.analyze(repository) }
    }

    @Test
    fun analyze_returnsAllSourcesInRevision() {
        val repository = repository {
            +revision("1") {
                +qualifiedSourcePathOf("src/Main.java").add {
                    +type("Main") { +function("main(String[])") {} }
                }
            }
            +revision("2") { +qualifiedSourcePathOf("src/Test.java").add { +type("Test") {} } }
            +revision("3") { +qualifiedSourcePathOf("src/Main.java").remove() }
        }

        val expectedSources =
            revisionListOf("1", "2", "3")
                .zip(
                    listOf(
                        sourceSetOf("src/Main.java"),
                        sourceSetOf("src/Main.java", "src/Test.java"),
                        sourceSetOf("src/Test.java")
                    )
                )

        assertEquals(
            expected = expectedSources.last().second,
            actual = create().analyze(repository).sources
        )

        for ((revisionId, expected) in expectedSources) {
            val actual = create(revisionId.toString()).analyze(repository).sources

            assertEquals(expected, actual)
        }
    }

    @Test
    fun reportToString_listsOneSourcePerLine() {
        val expected =
            """
            src/Main.java
            src/Test.java
        """.trimIndent()

        val actual = LsTreeReport(sourceSetOf("src/Main.java", "src/Test.java")).toString()

        assertEquals(expected, actual)
    }
}
