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

package org.chronolens

import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.apply
import org.chronolens.core.repository.PersistentRepository
import org.chronolens.core.subprocess.Subprocess.execute
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.ExpectedSystemExit
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.rules.TemporaryFolder
import java.io.BufferedReader
import kotlin.test.assertEquals
import kotlin.test.fail

@Ignore // TODO: figure out how to speed this up.
class MainTest {
    companion object {
        @ClassRule
        @JvmField
        val tmp = TemporaryFolder.builder().assureDeletion().build()
        val repoDir get() = tmp.root.absolutePath

        @BeforeClass
        @JvmStatic
        fun setupRepository() {
            val url = "https://github.com/google/guava.git"
            execute(tmp.root, "git", "clone", url, "./")
            Main.main("persist", "--repo-dir", repoDir)
        }

        @AfterClass
        @JvmStatic
        fun cleanRepository() {
            Main.main("clean", "--repo-dir", repoDir)
        }
    }

    @get:Rule
    val outRule = SystemOutRule().enableLog()

    @get:Rule
    val exitRule = ExpectedSystemExit.none()

    private fun readResource(resource: String): String =
        javaClass.getResourceAsStream(resource).bufferedReader()
            .use(BufferedReader::readText)

    @Test
    fun `test snapshot equals applied edits from history`() {
        val repository = PersistentRepository.load(tmp.root) ?: fail()
        val expected = repository.getSnapshot()

        val actual = SourceTree.empty()
        for ((_, _, _, edits) in repository.getHistory()) {
            actual.apply(edits)
        }

        assertEquals(expected, actual)
    }

    @Test
    fun `test no args prints help`() {
        val expected = readResource("expected-help.txt")

        Main.main()
        val actual = outRule.log

        assertEquals(expected, actual)
    }

    // TODO: figure out why the revision is not found.
    @Test
    fun `test list tree`() {
        val expected = readResource("expected-ls-tree.txt")

        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        Main.main("ls-tree", "--rev", revision, "--repo-dir", repoDir)
        val actual = outRule.log

        assertEquals(expected, actual)
    }

    @Test
    fun `test list tree invalid revision exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val revision = "*#&%^!"
        Main.main("ls-tree", "--rev", revision, "--repo-dir", repoDir)
    }

    @Test
    fun `test list tree non-existing revision exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val revision = "abcdefghijklmnopqrstuvwxyz0123456789"
        Main.main("ls-tree", "--rev", revision, "--repo-dir", repoDir)
    }

    @Test
    fun `test rev list`() {
        val expected = execute(
            tmp.root,
            "git",
            "rev-list",
            "--first-parent",
            "--reverse",
            "HEAD",
            "--",
            ""
        ).get()

        Main.main("rev-list", "--repo-dir", repoDir)
        val actual = outRule.log

        assertEquals(expected, actual)
    }

    // TODO: figure out why the revision is not found.
    @Test
    fun `test model`() {
        val expected = readResource("expected-model.txt")

        val id = "guava/src/com/google/common/eventbus/Dispatcher.java"
        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        Main.main("model", "--id", id, "--rev", revision, "--repo-dir", repoDir)
        val actual = outRule.log

        assertEquals(expected, actual)
    }

    @Test
    fun `test model invalid path exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val id = "guava/../guava/src/com/google/common/eventbus/Dispatcher.java"
        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        Main.main("model", "--id", id, "--rev", revision, "--repo-dir", repoDir)
    }

    @Test
    fun `test model invalid revision exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val id = "guava/src/com/google/common/eventbus/Dispatcher.java"
        val revision = "*#&%^!"
        Main.main("model", "--id", id, "--rev", revision, "--repo-dir", repoDir)
    }

    @Test
    fun `test model non-existing path exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val id = "guava/src/com/google/common/eventbus/NonExisting.java"
        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        Main.main("model", "--id", id, "--rev", revision, "--repo-dir", repoDir)
    }

    @Test
    fun `test model non-existing revision exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val id = "guava/src/com/google/common/eventbus/Dispatcher.java"
        val revision = "abcdefghijklmnopqrstuvwxyz0123456789"
        Main.main("model", "--id", id, "--rev", revision, "--repo-dir", repoDir)
    }

    @Test
    fun `test model existing path non-existing id exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val id = "guava/src/com/google/common/eventbus/Dispatcher.java:Missing"
        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        Main.main("model", "--id", id, "--rev", revision, "--repo-dir", repoDir)
    }
}
