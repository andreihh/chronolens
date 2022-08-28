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

import java.io.IOException
import java.io.UncheckedIOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.chronolens.test.core.model.revision

class FakeRepositoryStorageTest {
    private val storage = FakeRepositoryStorage()

    @Test
    fun writeHistory_persistsRevisions() {
        val history =
            listOf(
                revision("abc") { author("t1@test.com") },
                revision("def") { author("t2@test.com") },
                revision("ghi") { author("t3@test.com") }
            )

        storage.writeHistory(history.asSequence())

        assertEquals(expected = listOf("abc", "def", "ghi"), actual = storage.readHistoryIds())
        assertEquals(expected = history, actual = storage.readHistory().toList())
    }

    @Test
    fun allOperations_afterSetError_throw() {
        storage.setError(IOException("test"))

        assertFailsWith<IOException> { storage.readHistoryIds() }
        assertFailsWith<IOException> { storage.readHistory() }
        assertFailsWith<IOException> { storage.writeHistory(emptySequence()) }
    }

    @Test
    fun allOperations_afterSetAndClearError_succeed() {
        storage.setError(IOException("test"))
        storage.clearError()

        storage.readHistoryIds()
        storage.readHistory()
        storage.writeHistory(emptySequence())
    }

    @Test
    fun readHistory_iterateHistoryAfterSetError_throws() {
        storage.writeHistory(sequenceOf(revision("abc") {}))

        val history = storage.readHistory()

        storage.setError(IOException("test"))

        assertFailsWith<UncheckedIOException> { history.toList() }
    }
}
