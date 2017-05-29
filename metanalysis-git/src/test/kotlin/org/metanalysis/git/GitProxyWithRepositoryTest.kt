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

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import org.metanalysis.core.subprocess.Subprocess.execute
import org.metanalysis.core.versioning.RevisionNotFoundException
import org.metanalysis.core.versioning.VcsProxyFactory

import java.io.File

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GitProxyWithRepositoryTest : GitProxyTest() {
    companion object {
        @BeforeClass
        @JvmStatic fun cloneRepository() {
            val url = "https://github.com/andrei-heidelbacher/metanalysis.git"
            val gitDir = ".git"
            execute("git", "clone", "--bare", url, gitDir)
        }

        @AfterClass
        @JvmStatic fun cleanRepository() {
            val gitDir = ".git"
            File(gitDir).deleteRecursively()
        }
    }

    @Test fun `test detect`() {
        assertTrue(VcsProxyFactory.detect() is GitProxy)
    }

    @Test fun `test get revision`() {
        val head = git.getHead()
        val commit = git.getRevision("HEAD")
        val branch = git.getRevision("master")
        assertEquals(head, commit)
        assertEquals(head, branch)
        val invalidRevisions = listOf("0123456", "master-invalid", "gradle")
        for (rev in invalidRevisions) {
            assertFailsWith<RevisionNotFoundException> { git.getRevision(rev) }
        }
    }


    @Test fun `test list files`() {
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
        val actual = git.listFiles()
        assertEquals(expected, actual)
    }

    @Test fun `test get file`() {
        val expected = File("build.gradle").readText()
        val actual = git.getFile("HEAD", "metanalysis-git/build.gradle")
        assertEquals(expected, actual)
        assertNull(git.getFile("HEAD", "non-existent.txt"))
        assertFailsWith<RevisionNotFoundException> {
            git.getFile("invalid-revision", "metanalysis-git/build.gradle")
        }
    }

    @Test fun `test get file history`() {
        assertEquals(emptyList(), git.getFileHistory("non-existent.txt"))
        val history = git.getFileHistory("README.md")
        println(history.joinToString(separator = "\n"))
    }
}
