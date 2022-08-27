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

package org.chronolens.cli

import org.chronolens.cli.Main.Companion.main
import org.chronolens.core.versioning.VcsRevision
import org.chronolens.test.core.versioning.FakeVcsProxyFactory
import org.chronolens.test.core.versioning.vcsRevision
import org.junit.Rule
import org.junit.contrib.java.lang.system.ExpectedSystemExit
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals

class MainTest {
    @get:Rule val tmp: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()
    @get:Rule val outRule: SystemOutRule = SystemOutRule().enableLog()
    @get:Rule val exitRule: ExpectedSystemExit = ExpectedSystemExit.none()

    private val directory by lazy { tmp.root }

    @Test
    fun main_whenRevList_printsRevisions() {
        val vcsProxy = FakeVcsProxyFactory.createRepository(directory) {
            +vcsRevision {
                change("README.md", "Hello, world!")
            }
            +vcsRevision {
                delete("README.md")
            }
        }
        val expected =
            vcsProxy.getHistory().joinToString(separator = "\n", transform = VcsRevision::id) + "\n"

        main(arrayOf("rev-list", "--repository-root", directory.absolutePath))
        val actual = outRule.log

        assertEquals(expected, actual)
    }

    @Test
    fun main_whenLsTree_printsSources() {
        FakeVcsProxyFactory.createRepository(directory) {
            +vcsRevision {
                change("README.md", "Hello, world!")
                change("BUILD", "")
                change("src/Main.java", "")
                change("src/Test.java", "")
            }
        }
        val expected = setOf("src/Main.java", "src/Test.java").joinToString("\n") + "\n"

        main(arrayOf("ls-tree", "--repository-root", directory.absolutePath))
        val actual = outRule.log

        assertEquals(expected, actual)
    }
}