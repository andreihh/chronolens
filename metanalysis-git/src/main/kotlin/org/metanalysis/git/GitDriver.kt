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
import org.metanalysis.core.versioning.ObjectNotFoundException
import org.metanalysis.core.versioning.SubprocessException
import org.metanalysis.core.versioning.SubprocessException.SubprocessTerminatedException
import org.metanalysis.core.versioning.VersionControlSystem
import org.metanalysis.core.versioning.VersionControlSystem.Subprocess.Result
import org.metanalysis.core.versioning.VersionControlSystem.Subprocess.execute

import java.io.IOException
import java.util.Date

/** A module which integrates the `git` version control system. */
class GitDriver : VersionControlSystem() {
    private val formatOption: String = "--format=%at:%an"

    private fun objectNotFound(id: String): ObjectNotFoundException =
            ObjectNotFoundException("Not a valid object name '$id'!")

    @Throws(SubprocessException::class, IOException::class)
    private fun Result.get(): String = when (this) {
        is Result.Success -> input
        is Result.Error -> when {
            "Not a valid object name" in message
                    || "unknown revision" in message
                    || "bad revision" in message ->
                throw ObjectNotFoundException(message)
            else -> throw SubprocessTerminatedException(message)
        }
    }

    private fun String.spread(): Array<String> = split(' ').toTypedArray()

    private fun List<String>.pairUpLines(separator: Char): List<String> =
            (0 until size - 1 step 2).map { i ->
                "${get(i)}$separator${get(i + 1)}"
            }

    /**
     * Parse the commit from the following data format:
     * ```* commit <sha1>:<seconds since epoch>:<author name>```
     */
    private fun parseCommit(line: String): Commit {
        val (id, date, author) = line.removePrefix("commit ")
                .split(':', limit = 3)
        return Commit(id, author, Date(1000L * date.toLong()))
    }

    @Throws(IOException::class)
    override fun isSupported(): Boolean =
        execute("git", "--version") is Result.Success

    @Throws(IOException::class)
    override fun detectRepository(): Boolean {
        val result = execute("git", "cat-file", "-e", "HEAD")
        return when (result) {
            is Result.Success -> true
            is Result.Error ->
                if ("Not a git repository" in result.message) false
                else throw SubprocessTerminatedException(result.message)
        }
    }

    @Throws(IOException::class)
    override fun getHead(): Commit = getCommit("HEAD")

    @Throws(IOException::class)
    override fun getCommit(revision: String): Commit =
            execute("git", "rev-list", "-1", formatOption, "$revision^{commit}")
                    .get().lines()
                    .pairUpLines(':')
                    .singleOrNull()
                    ?.let(this::parseCommit)
                    ?: throw objectNotFound(revision)

    @Throws(IOException::class)
    override fun listFiles(revision: String): Set<String> =
            execute("git", "ls-tree", "--name-only", "-r", "$revision^{commit}")
                    .get().lines()
                    .filter(String::isNotBlank)
                    .toSet()

    @Throws(IOException::class)
    override fun getFile(revision: String, path: String): String =
            execute("git", "cat-file", "blob", "$revision^{commit}:$path").get()

    @Throws(IOException::class)
    override fun getFileHistory(
            revision: String,
            path: String
    ): List<Commit> = execute(
            *"git rev-list --first-parent --reverse $formatOption".spread(),
            "$revision^{commit}",
            "--",
            path
    ).get().lines()
            .pairUpLines(':')
            .map(this::parseCommit)
            .takeIf(List<Commit>::isNotEmpty)
            ?: throw objectNotFound("$revision:$path")
}
