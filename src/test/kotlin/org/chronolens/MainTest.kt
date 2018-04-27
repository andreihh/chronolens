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
import org.chronolens.core.repository.InteractiveRepository
import org.chronolens.core.repository.PersistentRepository
import org.chronolens.core.subprocess.Subprocess.execute
import org.chronolens.test.core.model.assertEquals
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.fail

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

    private fun readResource(resource: String): String =
        javaClass.getResourceAsStream(resource).bufferedReader()
            .use(BufferedReader::readText)

    private var out = ByteArrayOutputStream()

    private fun resetStdOut() {
        System.out.flush()
        out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))
    }

    private fun getStdOut(): String {
        System.out.flush()
        return out.toByteArray().toString(Charsets.UTF_8)
    }

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

        resetStdOut()
        Main.main()
        val actual = getStdOut()

        assertEquals(expected, actual)
    }

    @Test fun `test list tree`() {
        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        val expected = readResource("expected-ls-tree.txt")

        resetStdOut()
        Main.main("ls-tree", "--rev", revision)
        val actual = getStdOut()

        assertEquals(expected, actual)
    }

    @Test fun `test model`() {
        val id = "guava/src/com/google/common/hash/Crc32cHashFunction.java"
        val revision = "9fad64c2874ab1aa21d3ecad54f19ae4a25f27fd"
        val expected = readResource("expected-model.txt")

        resetStdOut()
        Main.main("model", "--id", id, "--rev", revision)
        val actual = getStdOut()

        assertEquals(expected, actual)
    }
}
