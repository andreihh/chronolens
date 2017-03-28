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

import org.metanalysis.core.versioning.Commit
import org.metanalysis.core.versioning.VersionControlSystem
import java.io.IOException

import java.io.InputStream

class GitVersionControlSystem : VersionControlSystem() {
    companion object {
        const val NAME: String = "git"
    }

    override val name: String
        get() = NAME

    private fun execute(vararg command: String): Process =
            ProcessBuilder()
                    .command(*command)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start()

    @Throws(IOException::class)
    private fun InputStream.readLines(): List<String> = use {
        readBytes().map(Byte::toChar)
                .joinToString(separator = "")
                .split("\n")
    }

    override fun getCommit(commitId: String): Commit {
        val process = execute("git", "show", "--name-only", commitId)
        val lines = process.inputStream.readLines()
        if (lines.size < 3) {
            throw IOException("")
        }
        val (id, author, date) = lines
        return Commit(id, author, date)
    }

    override fun getHead(): String {
        val process = execute("git", "rev-parse", "HEAD")
        return process.inputStream.readLines().firstOrNull()
                ?: throw IOException("")
    }

    override fun listFiles(commitId: String): Set<String> {
        val process = execute("git", "ls-tree", "--name-only", "-r", commitId)
        return process.inputStream.readLines()
                .filter(String::isNotBlank)
                .toSet()
    }

    override fun getFile(path: String, commitId: String): InputStream? {
        val process = execute("git", "show", "$commitId:$path")
        process.waitFor()
        return if (process.exitValue() != 0) {
            val error = process.errorStream.bufferedReader().readLine()
                    ?: throw IOException()
            if (error.contains("Path .* does not exist ")) {
                throw IOException(error)
            }
            null
        } else {
            process.inputStream
        }
    }

    override fun getFileHistory(path: String, commitId: String): List<String> {
        val process = execute(
                "git",
                "log",
                "--first-parent",
                "--pretty=format:\"%H\"",
                commitId,
                path
        )
        return process.inputStream.readLines()
                .map { it.drop(1).dropLast(1) }.asReversed()
    }
}
