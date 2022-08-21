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

package org.chronolens.test.core.repository

import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTreeEdit.Companion.apply
import org.chronolens.core.repository.CorruptedRepositoryException
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.Repository.HistoryProgressListener
import org.chronolens.test.core.model.assertEqualSourceTrees
import org.chronolens.test.core.model.function
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.model.sourceTree
import org.chronolens.test.core.model.type
import org.junit.Test
import kotlin.streams.toList
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

public abstract class AbstractRepositoryTest {
    protected abstract fun createRepository(vararg history: RevisionChangeSet): Repository

    private val repository by lazy {
        createRepository(
            revisionChangeSet {
                +sourceFile("src/Main.fake") {}
                +sourceFile("src/Worksheet.fake") {
                    +function("println()") {}
                }
                +sourceFile("src/Test.fake") {}
                +sourceFile("src/BuildVersion.fake") {}
                invalidate("src/Error.fake")
                invalidate("README.md")
            },
            revisionChangeSet {
                invalidate("src/Main.fake")
                invalidate("src/Worksheet.fake")
                delete("src/Test.fake")
                delete("src/BuildVersion.fake")
            },
            revisionChangeSet {
                +sourceFile("src/Main.fake") {
                    +type("Main") {}
                }
                +sourceFile("src/Worksheet.fake") {
                    +function("println(String)") {}
                }
                +sourceFile("src/Test.fake") {
                    +type("MainTest") {}
                }
            }
        )
    }

    @Test
    public fun getHeadId_returnsIdOfLastRevision() {
        assertEquals(expected = repository.listRevisions().last(), actual = repository.getHeadId())
    }

    @Test
    public fun listRevisions_returnsAllRevisions() {
        assertEquals(expected = 3, actual = repository.listRevisions().size)
    }

    @Test
    public fun listSources_returnsAllInterpretableSourcePathsAtRevision() {
        val expectedSources = listOf(
            setOf(
                "src/Main.fake",
                "src/Worksheet.fake",
                "src/Test.fake",
                "src/BuildVersion.fake",
                "src/Error.fake"
            ),
            setOf("src/Main.fake", "src/Worksheet.fake", "src/Error.fake"),
            setOf("src/Main.fake", "src/Worksheet.fake", "src/Test.fake", "src/Error.fake")
        ).map { it.map(::SourcePath).toSet() }

        val actualSources = repository.listRevisions().map(repository::listSources)

        assertEquals(expected = expectedSources, actual = actualSources)
        assertEquals(expected = expectedSources.last(), actual = repository.listSources())
    }

    @Test
    public fun listSources_whenRevisionDoesNotExist_throws() {
        assertFailsWith<IllegalArgumentException> {
            repository.listSources(RevisionId("invalid-revision"))
        }
    }

    @Test
    public fun getSource_returnsLatestValidSourceFileVersion() {
        val expectedSourceFile =
            listOf(
                sourceFile("src/Worksheet.fake") {
                    +function("println()") {}
                },
                sourceFile("src/Worksheet.fake") {
                    +function("println()") {}
                },
                sourceFile("src/Worksheet.fake") {
                    +function("println(String)") {}
                }
            )

        val actualSourceFile = repository.listRevisions().map { revisionId ->
            repository.getSource(SourcePath("src/Worksheet.fake"), revisionId)
        }

        assertEquals(expected = expectedSourceFile, actual = actualSourceFile)
        assertEquals(
            expected = expectedSourceFile.last(),
            actual = repository.getSource(SourcePath("src/Worksheet.fake"))
        )
    }

    @Test
    public fun getSource_whenSourcePathDoesNotExist_returnsNull() {
        val expectedSourceFile = listOf(
            sourceFile("src/Test.fake") {},
            null,
            sourceFile("src/Test.fake") { +type("MainTest") {} }
        )

        val actualSourceFile = repository.listRevisions().map { revisionId ->
            repository.getSource(SourcePath("src/Test.fake"), revisionId)
        }

        assertEquals(expected = expectedSourceFile, actual = actualSourceFile)
    }

    @Test
    public fun getSource_whenCannotParse_returnsNull() {
        for (revisionId in repository.listRevisions()) {
            assertNull(repository.getSource(SourcePath("README.md"), revisionId))
        }
    }

    @Test
    public fun getSource_whenNoValidVersionExists_returnsEmptySourceFile() {
        val emptySourceFile = SourceFile(SourcePath("src/Error.fake"))

        assertEquals(
            expected = emptySourceFile,
            actual = repository.getSource(SourcePath("src/Error.fake"))
        )
    }

    @Test
    public fun getSource_whenRevisionDoesNotExist_throws() {
        assertFailsWith<IllegalArgumentException> {
            repository.getSource(SourcePath("src/Main.fake"), RevisionId("invalid-revision"))
        }
    }

    @Test
    public fun getSnapshot_returnsAllSourceTrees() {
        val expectedSourceTrees = listOf(
            sourceTree {
                +sourceFile("src/Main.fake") {}
                +sourceFile("src/Worksheet.fake") {
                    +function("println()") {}
                }
                +sourceFile("src/Test.fake") {}
                +sourceFile("src/BuildVersion.fake") {}
                +sourceFile("src/Error.fake") {}
            },
            sourceTree {
                +sourceFile("src/Main.fake") {}
                +sourceFile("src/Worksheet.fake") {
                    +function("println()") {}
                }
                +sourceFile("src/Error.fake") {}
            },
            sourceTree {
                +sourceFile("src/Main.fake") { +type("Main") {} }
                +sourceFile("src/Test.fake") { +type("MainTest") {} }
                +sourceFile("src/Worksheet.fake") { +function("println(String)") {} }
                +sourceFile("src/Error.fake") {}
            }
        )

        val actualSourceTrees = repository.listRevisions().map(repository::getSnapshot)

        assertEquals(expected = expectedSourceTrees.size, actual = actualSourceTrees.size)
        for ((expectedSourceTree, actualSourceTree) in expectedSourceTrees.zip(actualSourceTrees)) {
            assertEqualSourceTrees(expected = expectedSourceTree, actual = actualSourceTree)
        }
        assertEqualSourceTrees(
            expected = expectedSourceTrees.last(),
            actual = repository.getSnapshot()
        )
    }

    @Test
    public fun getSnapshot_whenRevisionDoesNotExist_throws() {
        assertFailsWith<IllegalArgumentException> {
            repository.getSnapshot(RevisionId("invalid-revision"))
        }
    }

    @Test
    public fun getHistory_returnsRevisionsInOrder() {
        assertEquals(
            expected = repository.listRevisions(),
            actual = repository.getHistory().map(Revision::id).toList()
        )
    }

    @Test
    public fun getHistory_whenAppliedToEmptySourceTree_resultsInSnapshot() {
        val expectedSourceTrees = repository.listRevisions().map(repository::getSnapshot)

        val history = repository.getHistory().toList()

        assertEquals(expected = expectedSourceTrees.size, actual = history.size)
        val actualSourceTree = sourceTree {}
        for ((expectedSourceTree, revision) in expectedSourceTrees.zip(history)) {
            actualSourceTree.apply(revision.edits)
            assertEqualSourceTrees(expectedSourceTree, actualSourceTree)
        }
    }

    @Test
    public fun getHistory_whenProgressListenerIsProvided_invokesListener() {
        val listener = object : HistoryProgressListener {
            var count = -1
            val revisions = mutableListOf<RevisionId>()
            var ended = false

            override fun onStart(revisionCount: Int) {
                count = revisionCount
            }

            override fun onRevision(revision: Revision) {
                revisions += revision.id
            }

            override fun onEnd() {
                ended = true
            }
        }

        repository.getHistory(listener).toList()

        assertEquals(expected = 3, actual = listener.count)
        assertEquals(expected = repository.listRevisions(), actual = listener.revisions)
        assertTrue(listener.ended)
    }

    @Test
    public fun getHistory_returnsLazyCollection() {
        val listener = object : HistoryProgressListener {
            var started = false

            override fun onStart(revisionCount: Int) {
                started = true
            }

            override fun onRevision(revision: Revision) {}

            override fun onEnd() {}
        }

        repository.getHistory(listener)

        assertFalse(listener.started)
    }

    @Test
    public fun getHistoryStream_returnsSameHistory() {
        assertEquals(
            expected = repository.getHistory().toList(),
            actual = repository.getHistoryStream().toList()
        )
    }

    @Test
    public fun allOperations_whenEmptyRepository_throw() {
       val emptyRepository = createRepository()

        assertFailsWith<CorruptedRepositoryException> {
            emptyRepository.getHeadId()
        }
        assertFailsWith<CorruptedRepositoryException> {
            emptyRepository.listSources()
        }
        assertFailsWith<CorruptedRepositoryException> {
            emptyRepository.getSource(SourcePath("src/Main.fake"))
        }
        assertFailsWith<CorruptedRepositoryException> {
            emptyRepository.listRevisions()
        }
        assertFailsWith<CorruptedRepositoryException> {
            emptyRepository.getSnapshot()
        }
        assertFailsWith<CorruptedRepositoryException> {
            emptyRepository.getHistory()
        }
    }
}
