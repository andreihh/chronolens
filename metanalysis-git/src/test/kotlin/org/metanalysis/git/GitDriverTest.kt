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
import org.metanalysis.core.versioning.SubprocessException

import java.io.FileNotFoundException
import kotlin.concurrent.thread

import kotlin.test.assertNull

class GitDriverTest {
    private val git = GitDriver()

    private fun sleep(): Int {
        var i = 0
        for (j in 1 until 1000000000) {
            i += j * i - j
        }
        return i
    }

    @Test fun `test get head`() {
        val head = git.getHead()
        println(head)
    }

    @Test fun `test get commit`() {
        val commit = git.getCommit("HEAD")
        println(commit)
    }

    @Test fun `test get branch`() {
        val commit = git.getCommit("master")
        println(commit)
    }

    @Test(expected = SubprocessException::class)
    fun `test interrupt get commit throws`() {
        val gitThread = Thread.currentThread()
        thread { gitThread.interrupt() }
        sleep()
        git.getHead()
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get invalid commit throws`() {
        git.getCommit("00000000000")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get invalid branch throws`() {
        git.getCommit("master-non-existent")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get file as commit throws`() {
        println(git.getCommit("src"))
    }

    @Test fun `test list files from commit`() {
        val files = git.listFiles("HEAD")
        println(files)
    }

    @Test fun `test list files from branch`() {
        val files = git.listFiles("master")
        println(files)
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test list files from invalid commit throws`() {
        git.listFiles("0000000")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test list files from invalid branch throws`() {
        git.listFiles("master-non-existent")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test list files from file as commit throws`() {
        git.listFiles("src")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test list files from revision-directory throws`() {
        git.listFiles("HEAD:src")
    }

    @Test fun `test get file from commit`() {
        val fileContent = git.getFile("HEAD", "build.gradle")
        println(fileContent)
    }

    @Test fun `test get file from branch`() {
        val fileContent = git.getFile("master", "build.gradle")
        println(fileContent)
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get file from invalid commit throws`() {
        git.getFile("0000000", "build.gradle")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get file from invalid branch throws`() {
        git.getFile("master-non-existent", "build.gradle")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get file from file as commit throws`() {
        git.getFile("src", "build.gradle")
    }

    @Test fun `test get non-existent file from commit throws`() {
        assertNull(git.getFile("HEAD", "non-existent.txt"))
    }

    @Test fun `test get non-existent file from branch throws`() {
        assertNull(git.getFile("HEAD", "non-existent.txt"))
    }

    @Test fun `test get file history from commit`() {
        val history = git.getFileHistory("HEAD", "../README.md")
        println(history.joinToString(separator = "\n"))
    }

    @Test fun `test get file history from branch`() {
        val history = git.getFileHistory("master", "../README.md")
        println(history.joinToString(separator = "\n"))
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get file history from invalid commit throws`() {
        git.getFileHistory("000000", "build.gradle")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get file history from invalid branch throws`() {
        git.getFileHistory("master-non-existent", "build.gradle")
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test get file history from file as commit throws`() {
        git.getFileHistory("000000", "src")
    }

    @Test(expected = FileNotFoundException::class)
    fun `test get non-existent file history from commit throws`() {
        git.getFileHistory("HEAD", "non-existent.txt")
    }

    @Test(expected = FileNotFoundException::class)
    fun `test get non-existent file history from branch throws`() {
        git.getFileHistory("master", "non-existent.txt")
    }
}
