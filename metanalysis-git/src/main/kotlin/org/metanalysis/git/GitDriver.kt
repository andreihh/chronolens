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
import org.metanalysis.core.versioning.SubprocessException
import org.metanalysis.core.versioning.VersionControlSystem
import org.metanalysis.core.versioning.VersionControlSystem.Subprocess.Result
import org.metanalysis.core.versioning.VersionControlSystem.Subprocess.execute

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

    /**
     * Parse the commit from the following data format:
     * ```
     * commit <sha1>
     * seconds-since-epoch:author-name
     * ```
     */
    private fun parseCommit(firstLine: String, secondLine: String): Commit {
        val (_, id) = firstLine.split(' ', limit = 2)
        val (date, author) = secondLine.split(':', limit = 2)
        return Commit(id, author, Date(1000L * date.toLong()))
    }

    @Throws(IOException::class)
    override fun isSupported(): Boolean =
        execute("git", "--version") is Result.Success

    @Throws(IOException::class)
    override fun detectRepository(): Boolean  {
        val result = execute("git", "cat-file", "-e", "HEAD")
        return when (result) {
            is Result.Success -> true
            is Result.Error ->
                if ("Not a git repository" in result.message) false
                else throw IOException(result.message)
        }
    }

    @Throws(IOException::class)
    override fun getHead(): Commit = getCommit("HEAD")

    @Throws(IOException::class)
    override fun getCommit(revision: String): Commit {
        val result = execute("git", "rev-list", "-1", formatOption, revision)
        return when (result) {
            is Result.Success -> try {
                val (firstLine, secondLine) = result.input.split('\n')
                parseCommit(firstLine, secondLine)
            } catch (e: IndexOutOfBoundsException) {
                throw IOException("Can't parse commit from '${result.input}'")
            }
            is Result.Error -> throw IOException(result.message)
        }
    }

    @Throws(IOException::class)
    override fun listFiles(revision: String): Set<String> {
        val result = execute("git", "ls-tree", "--name-only", "-r", revision)
        return when (result) {
            is Result.Success ->
                result.input.split('\n').filter(String::isNotBlank).toSet()
            is Result.Error -> throw IOException(result.message)
        }
    }

    @Throws(IOException::class)
    override fun getFile(revision: String, path: String): String? {
        val result = execute("git", "cat-file", "blob", "$revision:$path")
        return when (result) {
            is Result.Success -> result.input
            is Result.Error ->
                if ("Not a valid object name" in result.message) null
                else throw IOException(result.message)
        }
    }

    @Throws(IOException::class)
    override fun getFileHistory(revision: String, path: String): List<Commit> {
        val result = execute(
                "git",
                "rev-list",
                "--first-parent",
                formatOption,
                "--reverse",
                revision,
                "--",
                path
        )
        return when (result) {
            is Result.Success -> {
                val lines = result.input.split('\n')
                val commits = arrayListOf<Commit>()
                for (i in 0 until lines.size - 1 step 2) {
                    commits += parseCommit(lines[i], lines[i + 1])
                }
                if (commits.isNotEmpty()) commits
                else throw FileNotFoundException(
                        "'$path' doesn't exist in '$revision' or its ancestors!"
                )
            }
            is Result.Error -> throw IOException(result.message)
        }
    }
}
