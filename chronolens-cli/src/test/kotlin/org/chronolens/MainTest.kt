/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.Project
import org.chronolens.core.repository.PersistentRepository
import org.chronolens.core.subprocess.Subprocess.execute
import org.chronolens.test.core.model.assertEquals
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.ExpectedSystemExit
import org.junit.contrib.java.lang.system.SystemOutRule
import java.io.BufferedReader
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.fail

@Ignore
class MainTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupRepository() {
            val url = "https://github.com/google/guava.git"
            execute("git", "clone", url, "./")
            Main.main("persist")
        }

        @AfterClass
        @JvmStatic
        fun cleanRepository() {
            Main.main("clean")
            File("./").listFiles().forEach { it.deleteRecursively() }
        }
    }

    @Rule
    @JvmField
    val outRule = SystemOutRule().enableLog()

    @Rule
    @JvmField
    val exitRule = ExpectedSystemExit.none()

    private fun readResource(resource: String): String =
        javaClass.getResourceAsStream(resource).bufferedReader()
            .use(BufferedReader::readText)

    @Test fun `test snapshot equals applied edits from history`() {
        val repository = PersistentRepository.load() ?: fail()
        val expected = repository.getSnapshot()

        val actual = Project.empty()
        for ((_, _, _, edits) in repository.getHistory()) {
            actual.apply(edits)
        }

        assertEquals(expected, actual)
    }

    @Test fun `test no args prints help`() {
        val expected = readResource("expected-help.txt")

        Main.main()
        val actual = outRule.log

        assertEquals(expected, actual)
    }

    @Test fun `test list tree`() {
        val expected = readResource("expected-ls-tree.txt")

        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        Main.main("ls-tree", "--rev", revision)
        val actual = outRule.log

        assertEquals(expected, actual)
    }

    @Test fun `test list tree invalid revision exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val revision = "*#&%^!"
        Main.main("ls-tree", "--rev", revision)
    }

    @Test fun `test list tree non-existing revision exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val revision = "abcdefghijklmnopqrstuvwxyz0123456789"
        Main.main("ls-tree", "--rev", revision)
    }

    @Test fun `test rev list`() {
        val expected = execute(
            "git", "rev-list", "--first-parent", "--reverse", "HEAD", "--", ""
        ).get()

        Main.main("rev-list")
        val actual = outRule.log

        assertEquals(expected, actual)
    }

    @Test fun `test model`() {
        val expected = readResource("expected-model.txt")

        val id = "guava/src/com/google/common/eventbus/Dispatcher.java"
        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        Main.main("model", "--id", id, "--rev", revision)
        val actual = outRule.log

        assertEquals(expected, actual)
    }

    @Test fun `test model invalid path exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val id = "guava/../guava/src/com/google/common/eventbus/Dispatcher.java"
        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        Main.main("model", "--id", id, "--rev", revision)
    }

    @Test fun `test model invalid revision exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val id = "guava/src/com/google/common/eventbus/Dispatcher.java"
        val revision = "*#&%^!"
        Main.main("model", "--id", id, "--rev", revision)
    }

    @Test fun `test model non-existing path exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val id = "guava/src/com/google/common/eventbus/NonExisting.java"
        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        Main.main("model", "--id", id, "--rev", revision)
    }

    @Test fun `test model non-existing revision exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val id = "guava/src/com/google/common/eventbus/Dispatcher.java"
        val revision = "abcdefghijklmnopqrstuvwxyz0123456789"
        Main.main("model", "--id", id, "--rev", revision)
    }

    @Test fun `test model existing path non-existing id exits`() {
        exitRule.expectSystemExitWithStatus(1)

        val id = "guava/src/com/google/common/eventbus/Dispatcher.java:Missing"
        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        Main.main("model", "--id", id, "--rev", revision)
    }
}
