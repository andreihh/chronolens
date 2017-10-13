/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.metanalysis.core.repository

import org.junit.After
import org.junit.Test

import org.metanalysis.core.repository.PersistentRepository.Companion.persist
import org.metanalysis.test.core.repository.assertEquals

import java.io.File

import kotlin.test.assertNull
import kotlin.test.fail

class PersistentRepositoryTest : RepositoryTest() {
    override fun createRepository(): PersistentRepository =
            InteractiveRepository.connect()?.persist()
                    ?: fail("Couldn't connect to VCS repository!")

    @Test fun `test load after clean returns null`() {
        PersistentRepository.clean()
        assertNull(PersistentRepository.load())
    }

    @Test fun `test load returns equal repository`() {
        val expected = repository
        val actual = PersistentRepository.load()
                ?: fail("Couldn't load persisted repository!")
        assertEquals(expected, actual)
    }

    @Test fun `test persist already persisted returns same repository`() {
        val expected = repository
        val actual = repository.persist()
        assertEquals(expected, actual)
    }

    @After fun cleanPersistedRepository() {
        check(File(".metanalysis").deleteRecursively()) {
            "Couldn't clean up the persisted repository!"
        }
    }
}
