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

package org.metanalysis.git

import org.junit.Test

import org.metanalysis.core.subprocess.Subprocess.execute
import org.metanalysis.core.versioning.RevisionNotFoundException
import org.metanalysis.core.versioning.VcsProxy

import java.io.File

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GitProxyTest {
    private val url = "https://github.com/andrei-heidelbacher/metanalysis.git"

    private fun withRepository(url: String, block: GitProxy.() -> Unit) {
        val gitDir = ".git"
        try {
            execute("git", "clone", "--bare", url, gitDir)
            GitProxy().block()
        } finally {
            File(gitDir).deleteRecursively()
        }
    }

    private fun withEmptyRepository(block: GitProxy.() -> Unit) {
        val gitDir = ".git"
        try {
            execute("git", "init")
            GitProxy().block()
        } finally {
            File(gitDir).deleteRecursively()
        }
    }

    @Test fun `test detect repository`() {
        assertNull(VcsProxy.detect())
        withRepository(url) {
            assertTrue(VcsProxy.detect() is GitProxy)
        }
    }

    @Test fun `test get revision`() {
        withRepository(url) {
            val head = getHead()
            val commit = getRevision("HEAD")
            val branch = getRevision("master")
            assertEquals(head, commit)
            assertEquals(head, branch)
            val invalidRevisions = listOf("0123456", "master-invalid", "gradle")
            for (rev in invalidRevisions) {
                assertFailsWith<RevisionNotFoundException> { getRevision(rev) }
            }
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `test get head from empty repository throws`() {
        withEmptyRepository {
            getHead()
        }
    }

    @Test fun `test list files`() {
        withRepository(url) {
            val expected = execute(
                    "find", "../",
                    "-path", "*/.git", "-prune", "-o",
                    "-path", "*/build", "-prune", "-o",
                    "-path", "../.idea", "-prune", "-o",
                    "-path", "../.gradle", "-prune", "-o",
                    "-type", "f", "-print"
            ).get().lines()
                    .map { it.removePrefix("../") }
                    .filter(String::isNotBlank)
                    .toSet()
            val actual = listFiles()
            assertEquals(expected, actual)
        }
    }

    @Test fun `test get file`() {
        withRepository(url) {
            val expected = File("build.gradle").readText()
            val actual = getFile("HEAD", "metanalysis-git/build.gradle")
            assertEquals(expected, actual)
            assertNull(getFile("HEAD", "non-existent.txt"))
            assertFailsWith<RevisionNotFoundException> {
                getFile("invalid-revision", "metanalysis-git/build.gradle")
            }
        }
    }

    @Test fun `test get file history`() {
        withRepository(url) {
            assertEquals(emptyList(), getFileHistory("non-existent.txt"))
            val history = getFileHistory("README.md")
            println(history.joinToString(separator = "\n"))
        }
    }
}
