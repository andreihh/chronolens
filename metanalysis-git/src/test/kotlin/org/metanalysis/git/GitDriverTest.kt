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

import org.junit.Ignore
import org.junit.Test

import org.metanalysis.core.versioning.RevisionNotFoundException

import java.io.FileNotFoundException

import kotlin.test.assertNull

class GitDriverTest {
    private val git = GitDriver()

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
        val files = git.listFiles(git.getHead())
        println(files)
    }

    @Test(expected = RevisionNotFoundException::class)
    fun `test list files from revision-directory throws`() {
        git.listFiles(git.getRevision("HEAD:src"))
    }

    @Test fun `test get file from commit`() {
        val fileContent = git.getFile(git.getHead(), "build.gradle")
        println(fileContent)
    }

    @Test fun `test get non-existent file from commit returns null`() {
        assertNull(git.getFile(git.getHead(), "non-existent.txt"))
    }

    @Test fun `test get file history from commit`() {
        val history = git.getFileHistory(git.getHead(), "../README.md")
        println(history.joinToString(separator = "\n"))
    }

    @Test(expected = FileNotFoundException::class)
    fun `test get non-existent file history from commit throws`() {
        git.getFileHistory(git.getHead(), "non-existent.txt")
    }
}
