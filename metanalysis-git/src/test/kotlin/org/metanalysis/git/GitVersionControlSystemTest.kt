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

class GitVersionControlSystemTest {
    private val git = GitVersionControlSystem()

    @Test(timeout = 500) fun `test get head`() {
        val head = git.getHead()
        println(head)
    }

    @Test(timeout = 500) fun `test list files`() {
        val files = git.listFiles(git.getHead())
        println(files)
    }

    @Test(timeout = 500) fun `test get commit`() {
        val commit = git.getCommit(git.getHead())
        println(commit)
    }

    @Test(timeout = 500) fun `test get file`() {
        val fileContent = git.getFile("build.gradle", git.getHead())
                ?.bufferedReader()
                ?.readLines()
                ?.joinToString(separator = "\n")
        println(fileContent)
    }

    @Test(timeout = 500) fun `test get file history`() {
        val history = git.getFileHistory("../README.md", git.getHead())
        println(history.joinToString(separator = "\n"))
    }
}
