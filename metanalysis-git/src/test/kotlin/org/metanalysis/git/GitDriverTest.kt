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

import org.metanalysis.core.versioning.RevisionNotFoundException
import org.metanalysis.core.subprocess.Subprocess.execute
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.File
import java.io.FileNotFoundException

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GitDriverTest {
    private val git = GitDriver()

    @Test fun `test detect repository`() {
        assertTrue(VersionControlSystem.detect() is GitDriver)
    }

    @Test fun `test get head`() {
        val head = git.getHead()
        println(head)
    }

    @Test fun `test get commit`() {
        val commit = git.getRevision("HEAD")
        println(commit)
    }

    @Test fun `test get branch`() {
        val commit = git.getRevision("master")
        println(commit)
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get invalid commit throws`() {
        git.getRevision("00000000000")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get invalid branch throws`() {
        git.getRevision("master-non-existent")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get file as commit throws`() {
        println(git.getRevision("src"))
    }

    @Test fun `test list files from commit`() {
        val expected = execute(
                "find", "./",
                "-path", "./.git", "-prune", "-o",
                "-path", "*/build", "-prune", "-o",
                "-path", "./.idea", "-prune", "-o",
                "-path", "./.gradle", "-prune", "-o",
                "-type", "f", "-print"
        ).get().lines()
                .map { it.removePrefix("./") }
                .filter(String::isNotBlank)
                .toSet()
        val actual = git.listFiles(git.getHead())
        assertEquals(expected, actual)
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test list files from revision-directory throws`() {
        git.listFiles(git.getRevision("HEAD:src"))
    }

    @Test fun `test get file from commit`() {
        val expected = File("metanalysis-git/build.gradle").readText()
        val actual = git.getFile(git.getHead(), "metanalysis-git/build.gradle")
        assertEquals(expected, actual)
    }

    @Test fun `test get non-existent file from commit returns null`() {
        val actual = git.getFile(git.getHead(), "non-existent.txt")
        assertNull(actual)
    }

    @Test fun `test get file history from commit`() {
        val history = git.getFileHistory(git.getHead(), "README.md")
        println(history.joinToString(separator = "\n"))
    }

    @Test(expected = FileNotFoundException::class)
    fun `test get non-existent file history from commit throws`() {
        git.getFileHistory(git.getHead(), "non-existent.txt")
    }
}
