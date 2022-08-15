/*
 * Copyright 2017-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.repository.RepositoryConnector.AccessMode.RANDOM_ACCESS
import kotlin.test.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InteractiveRepositoryTest : RepositoryTest() {
    @get:Rule val tmp: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val connector by lazy { RepositoryConnector.newConnector(tmp.root) }

    override fun createRepository(): Repository =
        connector.connect(RANDOM_ACCESS)

    @Test
    fun `test connect with empty repository returns null`() {
        resetVcsRepository()
        assertNull(connector.tryConnect(RANDOM_ACCESS))
    }

    // TODO: figure out if test needs to be replaced.
    /*@Test
    fun `test get source from invalid revision throws`() {
        assertFailsWith<IllegalArgumentException> {
            (repository as InteractiveRepository).getSource(
                path = SourcePath("src/Main.mock"),
                revisionId = "^-+"
            )
        }
    }*/
}
