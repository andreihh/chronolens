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
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.repository.AbstractRepositoryTest
import org.chronolens.test.core.repository.FakeRepositoryStorage
import org.chronolens.test.core.repository.RevisionChangeSet
import org.chronolens.test.core.repository.assertEqualRepositories
import org.chronolens.test.core.repository.repository
import org.chronolens.test.core.repository.revisionChangeSet
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class PersistentRepositoryTest : AbstractRepositoryTest() {
    @get:Rule val tmp: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val connector by lazy { RepositoryConnector.newConnector(tmp.root) }
    private val repositoryStorage = FakeRepositoryStorage()

    override fun createRepository(vararg history: RevisionChangeSet): Repository =
        if (history.isNotEmpty()) repository(*history).persist(repositoryStorage)
        else PersistentRepository(repositoryStorage)

    // TODO: move loadPersistent tests to RepositoryConnectorTest.
    @Test
    fun loadPersistent_afterDelete_returnsNull() {
        repository(revisionChangeSet {}).persist(connector.openOrCreate())

        connector.delete()

        assertNull(connector.tryConnect(FAST_HISTORY))
    }

    @Test
    fun loadPersistent_afterPersist_returnsEqualRepository() {
        val expected = repository(revisionChangeSet {}).persist(connector.openOrCreate())

        val actual = connector.connect(FAST_HISTORY)

        assertEqualRepositories(expected, actual)
    }

    @Test
    fun persist_whenNotYetPersisted_returnsSameRepository() {
        val expected = repository(revisionChangeSet {
            +sourceFile("src/Main.fake") {}
        })

        val actual = expected.persist(repositoryStorage)

        assertEqualRepositories(expected, actual)
    }

    @Test
    fun persist_whenAlreadyPersisted_returnsSameRepository() {
        val expected = createRepository(revisionChangeSet {
            +sourceFile("src/Main.fake") {}
        })

        val actual = expected.persist(repositoryStorage)

        assertEqualRepositories(expected, actual)
    }

    @Test
    fun persist_invokesProgressListener() {
        val repository = createRepository(
            revisionChangeSet {},
            revisionChangeSet {},
            revisionChangeSet {},
        )

        val listener = object : HistoryProgressListener {
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

        repository.persist(connector.openOrCreate(), listener)

        assertTrue(listener.ended)
        assertEquals(expected = repository.listRevisions(), actual = listener.revisions)
    }
}
