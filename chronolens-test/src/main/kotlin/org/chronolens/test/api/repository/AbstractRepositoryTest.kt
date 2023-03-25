/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.api.repository

import kotlin.streams.toList
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.chronolens.api.repository.Repository
import org.chronolens.api.repository.Repository.HistoryProgressListener
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.chronolens.model.SourceTreeEdit.Companion.apply
import org.chronolens.test.model.assertEqualSourceTrees
import org.chronolens.test.model.function
import org.chronolens.test.model.sourceFile
import org.chronolens.test.model.sourceTree
import org.chronolens.test.model.type
import org.chronolens.test.model.variable
import org.junit.Test

public abstract class AbstractRepositoryTest {
  protected abstract fun createRepository(vararg history: RevisionChangeSet): Repository

  protected open val shouldFailIfClosed: Boolean
    get() = true

  private val repository by lazy {
    createRepository(
      revisionChangeSet {
        +sourceFile("src/Main.kts") {}
        +sourceFile("src/Worksheet.kts") { +function("println()") {} }
        +sourceFile("src/Test.kts") {}
        +sourceFile("src/BuildVersion.kts") { +variable("VERSION") {} }
        invalidate("src/Error.kts")
        +sourceFile("src/Delete.kts") {}
        invalidate("README.md")
      },
      revisionChangeSet {
        invalidate("src/Main.kts")
        invalidate("src/Worksheet.kts")
        invalidate("src/BuildVersion.kts")
        delete("src/Test.kts")
        delete("src/Delete.kts")
      },
      revisionChangeSet {
        +sourceFile("src/Main.kts") { +type("Main") {} }
        +sourceFile("src/Worksheet.kts") { +function("println(String)") {} }
        +sourceFile("src/Test.kts") { +type("MainTest") {} }
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
    val expectedSources =
      listOf(
        sourceSetOf(
          "src/Main.kts",
          "src/Worksheet.kts",
          "src/Test.kts",
          "src/BuildVersion.kts",
          "src/Error.kts",
          "src/Delete.kts"
        ),
        sourceSetOf("src/Main.kts", "src/Worksheet.kts", "src/BuildVersion.kts", "src/Error.kts"),
        sourceSetOf(
          "src/Main.kts",
          "src/Worksheet.kts",
          "src/Test.kts",
          "src/BuildVersion.kts",
          "src/Error.kts"
        )
      )

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
        sourceFile("src/Worksheet.kts") { +function("println()") {} },
        sourceFile("src/Worksheet.kts") { +function("println()") {} },
        sourceFile("src/Worksheet.kts") { +function("println(String)") {} }
      )

    val actualSourceFile =
      repository.listRevisions().map { revisionId ->
        repository.getSource(SourcePath("src/Worksheet.kts"), revisionId)
      }

    assertEquals(expected = expectedSourceFile, actual = actualSourceFile)
    assertEquals(
      expected = expectedSourceFile.last(),
      actual = repository.getSource(SourcePath("src/Worksheet.kts"))
    )
  }

  @Test
  public fun getSource_whenSourcePathDoesNotExist_returnsNull() {
    val expectedSourceFile =
      listOf(
        sourceFile("src/Test.kts") {},
        null,
        sourceFile("src/Test.kts") { +type("MainTest") {} }
      )

    val actualSourceFile =
      repository.listRevisions().map { revisionId ->
        repository.getSource(SourcePath("src/Test.kts"), revisionId)
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
    val emptySourceFile = SourceFile(SourcePath("src/Error.kts"))

    assertEquals(
      expected = emptySourceFile,
      actual = repository.getSource(SourcePath("src/Error.kts"))
    )
  }

  @Test
  public fun getSource_whenInvalidAndNotChangedInRevision_returnsLatestValidSourceFileVersion() {
    val expectedSourceFile =
      listOf(
        sourceFile("src/BuildVersion.kts") { +variable("VERSION") {} },
        sourceFile("src/BuildVersion.kts") { +variable("VERSION") {} },
        sourceFile("src/BuildVersion.kts") { +variable("VERSION") {} },
      )

    val actualSourceFile =
      repository.listRevisions().map { revisionId ->
        repository.getSource(SourcePath("src/BuildVersion.kts"), revisionId)
      }

    assertEquals(expected = expectedSourceFile, actual = actualSourceFile)
    assertEquals(
      expected = expectedSourceFile.last(),
      actual = repository.getSource(SourcePath("src/BuildVersion.kts"))
    )
  }

  @Test
  public fun getSource_whenRevisionDoesNotExist_throws() {
    assertFailsWith<IllegalArgumentException> {
      repository.getSource(SourcePath("src/Main.kts"), RevisionId("invalid-revision"))
    }
  }

  @Test
  public fun getSnapshot_returnsAllSourceTrees() {
    val expectedSourceTrees =
      listOf(
        sourceTree {
          +sourceFile("src/Main.kts") {}
          +sourceFile("src/Worksheet.kts") { +function("println()") {} }
          +sourceFile("src/Test.kts") {}
          +sourceFile("src/BuildVersion.kts") { +variable("VERSION") {} }
          +sourceFile("src/Error.kts") {}
          +sourceFile("src/Delete.kts") {}
        },
        sourceTree {
          +sourceFile("src/Main.kts") {}
          +sourceFile("src/Worksheet.kts") { +function("println()") {} }
          +sourceFile("src/BuildVersion.kts") { +variable("VERSION") {} }
          +sourceFile("src/Error.kts") {}
        },
        sourceTree {
          +sourceFile("src/Main.kts") { +type("Main") {} }
          +sourceFile("src/Test.kts") { +type("MainTest") {} }
          +sourceFile("src/Worksheet.kts") { +function("println(String)") {} }
          +sourceFile("src/BuildVersion.kts") { +variable("VERSION") {} }
          +sourceFile("src/Error.kts") {}
        }
      )

    val actualSourceTrees = repository.listRevisions().map(repository::getSnapshot)

    assertEquals(expected = expectedSourceTrees.size, actual = actualSourceTrees.size)
    for ((expectedSourceTree, actualSourceTree) in expectedSourceTrees.zip(actualSourceTrees)) {
      assertEqualSourceTrees(expected = expectedSourceTree, actual = actualSourceTree)
    }
    assertEqualSourceTrees(expected = expectedSourceTrees.last(), actual = repository.getSnapshot())
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
    val listener =
      object : HistoryProgressListener {
        var count = -1
        val revisions = mutableListOf<RevisionId>()
        var ended = false

        override fun onStart(revisionCount: Int) {
          assertEquals(expected = -1, actual = count)
          count = revisionCount
        }

        override fun onRevision(revision: Revision) {
          assertNotEquals(illegal = -1, actual = count)
          assertFalse(ended)
          revisions += revision.id
        }

        override fun onEnd() {
          assertEquals(expected = count, actual = revisions.size)
          ended = true
        }
      }

    repository.getHistory(listener).toList()

    assertTrue(listener.ended)
    assertEquals(expected = repository.listRevisions(), actual = listener.revisions)
  }

  @Test
  public fun getHistory_returnsLazyCollection() {
    val listener =
      object : HistoryProgressListener {
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
  public fun close_isIdempotent() {
    repository.close()
    repository.close()
  }

  @Test
  public fun allOperations_whenEmptyRepository_throw() {
    val emptyRepository = createRepository()

    assertFailsWith<IllegalStateException> { emptyRepository.getHeadId() }
    assertFailsWith<IllegalStateException> { emptyRepository.listSources() }
    assertFailsWith<IllegalStateException> { emptyRepository.getSource(SourcePath("src/Main.kts")) }
    assertFailsWith<IllegalStateException> { emptyRepository.listRevisions() }
    assertFailsWith<IllegalStateException> { emptyRepository.getSnapshot() }
    assertFailsWith<IllegalStateException> { emptyRepository.getHistory().toList() }
  }

  @Test
  public fun allOperations_whenClosedRepository_throw() {
    if (!shouldFailIfClosed) {
      return
    }

    repository.close()

    assertFailsWith<IllegalStateException> { repository.getHeadId() }
    assertFailsWith<IllegalStateException> { repository.listSources() }
    assertFailsWith<IllegalStateException> { repository.getSource(SourcePath("src/Main.kts")) }
    assertFailsWith<IllegalStateException> { repository.listRevisions() }
    assertFailsWith<IllegalStateException> { repository.getSnapshot() }
    assertFailsWith<IllegalStateException> { repository.getHistory().toList() }
  }
}
