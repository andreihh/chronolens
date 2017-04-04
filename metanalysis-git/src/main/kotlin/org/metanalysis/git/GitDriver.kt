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
import org.metanalysis.core.versioning.RevisionNotFoundException
import org.metanalysis.core.versioning.VersionControlSystem
import org.metanalysis.core.versioning.VersionControlSystem.Subprocess.Companion.execute

import java.io.FileNotFoundException
import java.io.IOException
import java.util.Date

/** A module which integrates the `git` version control system. */
class GitDriver : VersionControlSystem() {
    companion object {
        /** The `git` version control system name. */
        const val NAME: String = "git"
    }

    override val name: String = NAME

    private val formatOption: String = "--format=%at:%an"

    private fun parseCommit(lines: Pair<String, String>): Commit {
        // "commit <sha1>"
        // "seconds-since-epoch:author-name"
        val (_, id) = lines.first.split(' ')
        val (date, author) = lines.second.split(':', limit = 2)
        return Commit(id, author, Date(1000L * date.toLong()))
    }

    private fun fileNotFound(
            revision: String,
            path: String
    ): FileNotFoundException =
            FileNotFoundException("Path '$path' doesn't exist in '$revision'!")

    @Throws(IOException::class)
    private fun validateRevision(revision: String) {
        if (!execute("git", "cat-file", "-e", "$revision^{commit}")) {
            throw RevisionNotFoundException(revision)
        }
    }

    @Throws(IOException::class)
    override fun isSupported(): Boolean = execute("git", "--version")

    @Throws(IOException::class)
    override fun detectRepository(): Boolean =
            execute("git", "status", "--porcelain")

    @Throws(IOException::class)
    override fun getHead(): Commit = getCommit("HEAD")

    @Throws(IOException::class)
    override fun getCommit(
            revision: String
    ): Commit = object : Subprocess<Commit>() {
        init {
            validateRevision(revision)
        }

        override val command: List<String> =
                listOf("git", "rev-list", "-1", formatOption, revision)

        override fun onSuccess(input: String): Commit {
            val lines = input.split('\n')
            return parseCommit(lines[0] to lines[1])
        }
    }.run()

    @Throws(IOException::class)
    override fun listFiles(
            revision: String
    ): Set<String> = object : Subprocess<Set<String>>() {
        init {
            validateRevision(revision)
        }

        override val command: List<String> =
                listOf("git", "ls-tree", "--name-only", "-r", revision)

        override fun onSuccess(input: String): Set<String> =
                input.split('\n').filter(String::isNotBlank).toSet()
    }.run()

    @Throws(IOException::class)
    override fun getFile(
            revision: String,
            path: String
    ): String = object : Subprocess<String>() {
        init {
            validateRevision(revision)
        }

        override val command: List<String> =
                listOf("git", "cat-file", "blob", "$revision:$path")

        override fun onSuccess(input: String): String = input

        @Throws(IOException::class)
        override fun onError(error: String): Nothing = when {
            "Not a valid object name $revision:$path" in error ->
                throw fileNotFound(revision, path)
            else -> super.onError(error)
        }
    }.run()

    @Throws(IOException::class)
    override fun getFileHistory(
            revision: String,
            path: String
    ): List<Commit> = object : Subprocess<List<Commit>>() {
        init {
            validateRevision(revision)
        }

        override val command: List<String> = listOf(
                "git",
                "rev-list",
                "--first-parent",
                formatOption,
                "--reverse",
                revision,
                "--",
                path
        )

        @Throws(FileNotFoundException::class)
        override fun onSuccess(input: String): List<Commit> {
            val lines = input.split('\n')
            val commitLines = (0 until lines.size step 2).map(lines::get)
                    .zip((1 until lines.size step 2).map(lines::get))
            val commits = commitLines.map(this@GitDriver::parseCommit)
            return if (commits.isNotEmpty()) commits
            else throw fileNotFound(revision, path)
        }
    }.run()
}
