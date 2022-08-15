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
import kotlin.test.assertTrue
import org.chronolens.core.model.RevisionId
import org.chronolens.core.repository.PersistentRepository.Companion.persist
import org.chronolens.core.repository.PersistentRepository.ProgressListener
import org.chronolens.core.repository.RepositoryConnector.AccessMode.FAST_HISTORY
import org.chronolens.core.repository.RepositoryConnector.AccessMode.RANDOM_ACCESS
import org.chronolens.test.core.repository.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PersistentRepositoryTest : RepositoryTest() {
    @get:Rule val tmp: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    override fun createRepository(): PersistentRepository =
        RepositoryConnector.connect(RANDOM_ACCESS, tmp.root).persist(tmp.root)

    @Test
    fun `test load after clean returns null`() {
        PersistentRepository.clean(tmp.root)
        assertNull(RepositoryConnector.tryConnect(FAST_HISTORY, tmp.root))
    }

    @Test
    fun `test load returns equal repository`() {
        val expected = repository
        val actual = RepositoryConnector.connect(FAST_HISTORY, tmp.root)
        assertEquals(expected, actual)
    }

    @Test
    fun `test persist already persisted returns same repository`() {
        val expected = repository
        val actual = repository.persist(tmp.root)
        assertEquals(expected, actual)
    }

    private enum class ProgressListenerState {
        IDLE,
        HISTORY,
        DONE
    }

    @Test
    fun `test progress listener`() {
        val listener =
            object : ProgressListener {
                var state = ProgressListenerState.IDLE
                    private set

                private val revisions = mutableListOf<RevisionId>()

                override fun onHistoryStart(revisionCount: Int) {
                    revisions += repository.listRevisions()
                    revisions.reverse()
                    assertEquals(ProgressListenerState.IDLE, state)
                    assertEquals(revisions.size, revisionCount)
                    state = ProgressListenerState.HISTORY
                }

                override fun onRevisionPersisted(revisionId: RevisionId) {
                    assertEquals(ProgressListenerState.HISTORY, state)
                    assertEquals(revisions.last(), revisionId)
                    revisions.removeAt(revisions.size - 1)
                }

                override fun onHistoryEnd() {
                    assertEquals(ProgressListenerState.HISTORY, state)
                    assertTrue(revisions.isEmpty())
                    state = ProgressListenerState.DONE
                }
            }

        RepositoryConnector.connect(RANDOM_ACCESS, tmp.root).persist(tmp.root, listener)

        assertEquals(ProgressListenerState.DONE, listener.state)
    }
}
