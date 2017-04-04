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

import java.io.FileNotFoundException
import java.io.IOException
import java.util.Date

class GitDriver : VersionControlSystem() {
    companion object {
        const val NAME: String = "git"
    }

    override val name: String = NAME

    @Throws(IOException::class)
    private fun execute(vararg command: String): Int = try {
        ProcessBuilder().command(*command).start().waitFor()
    } catch (e: InterruptedException) {
        throw IOException(e)
    }

    private val formatOption: String = "--format=%at:%an"

    private fun parseCommit(lines: Pair<String, String>): Commit {
        // "commit <sha1>"
        // "seconds-since-epoch:author-name"
        val (_, id) = lines.first.split(' ')
        val (date, author) = lines.second.split(':', limit = 2)
        return Commit(id, author, Date(1000L * date.toLong()))
    }

    private fun fileNotFound(
            revisionId: String,
            path: String
    ): FileNotFoundException = FileNotFoundException(
            "Path '$path' doesn't exist in '$revisionId'!"
    )

    @Throws(IOException::class)
    private fun validateRevisionId(revisionId: String) {
        val exitCode = execute("git", "cat-file", "-e", "$revisionId^{commit}")
        require(exitCode == 0) { "Invalid revision id '$revisionId'!" }
    }

    @Throws(IOException::class)
    override fun isSupported(): Boolean = execute("git", "--version") == 0

    @Throws(IOException::class)
    override fun detectRepository(): Boolean =
            execute("git", "status", "--porcelain") == 0

    @Throws(IOException::class)
    override fun getHead(): Commit = getCommit("HEAD")

    @Throws(IOException::class)
    override fun getCommit(
            revisionId: String
    ): Commit = object : Subprocess<Commit>() {
        init {
            validateRevisionId(revisionId)
        }

        override val command: List<String> =
                listOf("git", "rev-list", "-1", formatOption, revisionId)

        override fun onSuccess(input: String): Commit {
            val lines = input.split('\n')
            return parseCommit(lines[0] to lines[1])
        }
    }.run()

    @Throws(IOException::class)
    override fun listFiles(
            revisionId: String
    ): Set<String> = object : Subprocess<Set<String>>() {
        init {
            validateRevisionId(revisionId)
        }

        override val command: List<String> =
                listOf("git", "ls-tree", "--name-only", "-r", revisionId)

        override fun onSuccess(input: String): Set<String> =
                input.split('\n').filter(String::isNotBlank).toSet()
    }.run()

    @Throws(FileNotFoundException::class, IOException::class)
    override fun getFile(
            revisionId: String,
            path: String
    ): String = object : Subprocess<String>() {
        init {
            validateRevisionId(revisionId)
        }

        override val command: List<String> =
                listOf("git", "cat-file", "blob", "$revisionId:$path")

        override fun onSuccess(input: String): String = input

        @Throws(FileNotFoundException::class, IOException::class)
        override fun onError(error: String): Nothing = when {
            "Not a valid object name $revisionId:$path" in error ->
                throw fileNotFound(revisionId, path)
            else -> super.onError(error)
        }
    }.run()

    @Throws(FileNotFoundException::class, IOException::class)
    override fun getFileHistory(
            revisionId: String,
            path: String
    ): List<Commit> = object : Subprocess<List<Commit>>() {
        init {
            validateRevisionId(revisionId)
        }

        override val command: List<String> = listOf(
                "git",
                "rev-list",
                "--first-parent",
                formatOption,
                revisionId,
                "--",
                path
        )

        @Throws(FileNotFoundException::class)
        override fun onSuccess(input: String): List<Commit> {
            val lines = input.split('\n')
            val commitLines = (0 until lines.size step 2).map(lines::get)
                    .zip((1 until lines.size step 2).map(lines::get))
            val commits = commitLines.map(this@GitDriver::parseCommit)
            return if (commits.isNotEmpty()) commits.asReversed()
            else throw fileNotFound(revisionId, path)
        }
    }.run()
}
