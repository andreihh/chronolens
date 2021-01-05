/*
 * Copyright 2018-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.git

import org.chronolens.core.versioning.VcsProxyFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertFailsWith
import kotlin.test.fail

class GitProxyWithEmptyRepositoryTest {
    @get:Rule val tmp = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun initRepository() {
        init(tmp.root)
    }

    @Test
    fun `test head after connect to empty repository throws`() {
        val git = VcsProxyFactory.detect(tmp.root) ?: fail()
        assertFailsWith<IllegalStateException> {
            git.getHead()
        }
    }

    @Test
    fun `test head after create empty repository throws`() {
        val git = GitProxy(tmp.root, "")
        assertFailsWith<IllegalStateException> {
            git.getHead()
        }
    }
}
