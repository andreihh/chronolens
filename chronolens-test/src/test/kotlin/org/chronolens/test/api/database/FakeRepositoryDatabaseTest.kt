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

package org.chronolens.test.api.database

import java.io.IOException
import java.io.UncheckedIOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.chronolens.model.RevisionId
import org.chronolens.test.model.revision

// TODO: add tests for reports and for appending history.
// TODO: add an AbstractDatabaseTest
class FakeRepositoryDatabaseTest {
  private val database = FakeRepositoryDatabase()

  @Test
  fun writeHistory_persistsRevisions() {
    val history =
      listOf(
        revision("abc") { author("t1@test.com") },
        revision("def") { author("t2@test.com") },
        revision("ghi") { author("t3@test.com") },
      )

    database.writeHistory(history.asSequence())

    assertEquals(
      expected = listOf("abc", "def", "ghi"),
      actual = database.readHistoryIds().map(RevisionId::toString),
    )
    assertEquals(expected = history, actual = database.readHistory().toList())
  }

  @Test
  fun allOperations_afterSetError_throw() {
    database.setError(IOException("test"))

    assertFailsWith<IOException> { database.readHistoryIds() }
    assertFailsWith<IOException> { database.readHistory() }
    assertFailsWith<IOException> { database.writeHistory(emptySequence()) }
  }

  @Test
  fun allOperations_afterSetAndClearError_succeed() {
    database.setError(IOException("test"))
    database.clearError()

    database.readHistoryIds()
    database.readHistory()
    database.writeHistory(emptySequence())
  }

  @Test
  fun readHistory_iterateHistoryAfterSetError_throws() {
    database.writeHistory(sequenceOf(revision("abc") {}))

    val history = database.readHistory()

    database.setError(IOException("test"))

    assertFailsWith<UncheckedIOException> { history.toList() }
  }
}
