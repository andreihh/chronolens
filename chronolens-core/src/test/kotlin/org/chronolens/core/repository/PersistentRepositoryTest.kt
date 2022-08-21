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

import org.chronolens.core.model.Revision
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.chronolens.core.model.RevisionId
import org.chronolens.core.repository.Repository.HistoryProgressListener
import org.chronolens.core.repository.RepositoryConnector.AccessMode.FAST_HISTORY
import org.chronolens.core.repository.RepositoryConnector.AccessMode.RANDOM_ACCESS
import org.chronolens.test.core.repository.assertEqualRepositories
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PersistentRepositoryTest : RepositoryTest() {
    @get:Rule val tmp: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val connector by lazy { RepositoryConnector.newConnector(tmp.root) }

    override fun createRepository(): Repository =
        connector.connect(RANDOM_ACCESS).persist(connector.openOrCreate())

    @Test
    fun `test load after clean returns null`() {
        connector.delete()
        assertNull(connector.tryConnect(FAST_HISTORY))
    }

    @Test
    fun `test load returns equal repository`() {
        val expected = repository
        val actual = connector.connect(FAST_HISTORY)
        assertEqualRepositories(expected, actual)
    }

    @Test
    fun `test persist already persisted returns same repository`() {
        val expected = repository
        val actual = repository.persist(connector.openOrCreate())
        assertEqualRepositories(expected, actual)
    }

    private enum class ProgressListenerState {
        IDLE,
        HISTORY,
        DONE
    }

    @Test
    fun `test progress listener`() {
        val listener =
            object : HistoryProgressListener {
                var state = ProgressListenerState.IDLE
                    private set

                private val revisions = mutableListOf<RevisionId>()

                override fun onStart(revisionCount: Int) {
                    revisions += repository.listRevisions()
                    revisions.reverse()
                    assertEquals(ProgressListenerState.IDLE, state)
                    assertEquals(revisions.size, revisionCount)
                    state = ProgressListenerState.HISTORY
                }

                override fun onRevision(revision: Revision) {
                    assertEquals(ProgressListenerState.HISTORY, state)
                    assertEquals(revisions.last(), revision.id)
                    revisions.removeAt(revisions.size - 1)
                }

                override fun onEnd() {
                    assertEquals(ProgressListenerState.HISTORY, state)
                    assertTrue(revisions.isEmpty())
                    state = ProgressListenerState.DONE
                }
            }

        connector.connect(RANDOM_ACCESS).persist(connector.openOrCreate(), listener)

        assertEquals(ProgressListenerState.DONE, listener.state)
    }
}
