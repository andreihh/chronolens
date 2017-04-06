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

    private val SubprocessException.invalidObjectName: Boolean
        get() = message?.contains("Not a valid object name") ?: false

    private val SubprocessException.invalidRepository: Boolean
        get() = message?.contains("Not a git repository") ?: false

    /**
     * Parse the commit from the following data format:
     * ```
     * commit <sha1>
     * seconds-since-epoch:author-name
     * ```
     */
    private fun parseCommit(firstLine: String, secondLine: String): Commit {
        val (_, id) = firstLine.split(' ')
        val (date, author) = secondLine.split(':', limit = 2)
        return Commit(id, author, Date(1000L * date.toLong()))
    }

    @Throws(IOException::class)
    private fun validateRevision(revision: String) {
        try {
            execute("git", "cat-file", "-e", "$revision^{commit}")
        } catch (e: SubprocessException) {
            if (e.invalidObjectName) throw RevisionNotFoundException(revision)
            else throw e
        }
    }

    @Throws(IOException::class)
    override fun isSupported(): Boolean = try {
        execute("git", "--version")
        true
    } catch (e: SubprocessException) {
        if (e.cause is InterruptedException) throw e
        else false
    }

    @Throws(IOException::class)
    override fun detectRepository(): Boolean = try {
        execute("git", "cat-file", "-e", "HEAD")
        true
    } catch (e: SubprocessException) {
        if (e.invalidRepository) false
        else throw e
    }

    @Throws(IOException::class)
    override fun getHead(): Commit = getCommit("HEAD")

    @Throws(IOException::class)
    override fun getCommit(revision: String): Commit {
        validateRevision(revision)
        val input = execute("git", "rev-list", "-1", formatOption, revision)
        val lines = input.split('\n')
        return parseCommit(lines[0], lines[1])
    }

    @Throws(IOException::class)
    override fun listFiles(revision: String): Set<String> {
        validateRevision(revision)
        val input = execute("git", "ls-tree", "--name-only", "-r", revision)
        return input.split('\n').filter(String::isNotBlank).toSet()
    }

    @Throws(IOException::class)
    override fun getFile(revision: String, path: String): String? {
        validateRevision(revision)
        return try {
            execute("git", "cat-file", "blob", "$revision:$path")
        } catch (e: SubprocessException) {
            if (e.invalidObjectName) null
            else throw e
        }
    }

    @Throws(IOException::class)
    override fun getFileHistory(revision: String, path: String): List<Commit> {
        validateRevision(revision)
        val input = execute(
                "git",
                "rev-list",
                "--first-parent",
                formatOption,
                "--reverse",
                revision,
                "--",
                path
        )
        val lines = input.split('\n')
        val evenLines = (0 until lines.size step 2).map(lines::get)
        val oddLines = (1 until lines.size step 2).map(lines::get)
        val commits = evenLines.zip(oddLines, this::parseCommit)
        return if (commits.isNotEmpty()) commits
        else throw FileNotFoundException(
                "'$path' doesn't exist in '$revision' or its ancestors!"
        )
    }
}
