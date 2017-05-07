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

import org.metanalysis.core.subprocess.Subprocess.execute
import org.metanalysis.core.versioning.RevisionNotFoundException
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.IOException
import java.util.Date

/** A module which integrates the `git` version control system. */
class GitDriver : VersionControlSystem() {
    private val format = "--format=%at:%an"
    private val headId = "HEAD"

    private fun List<String>.formatCommit(): String? =
            "${get(0)}:${get(1)}".removePrefix("commit ")

    private fun validateRevision(revisionId: String) {
        val result = execute("git", "cat-file", "-e", "$revisionId^{commit}")
        if (!result.isSuccess) {
            throw RevisionNotFoundException(revisionId)
        }
    }

    private fun parseCommit(lines: Pair<String, String>): Revision {
        val id = lines.first.removePrefix("commit ")
        val (rawDate, author) = lines.second.split(':', limit = 2)
        val date = Date(rawDate.toLong() * 1000)
        return getRevision("")//Revision(id, date, author)
    }

    private fun String.pairUpLines(): List<Pair<String, String>> {
        val lines = lines()
        return (0 until lines.size - 1).map { i ->
            Pair(lines[i], lines[i + 1])
        }
    }

    @Throws(IOException::class)
    override fun isSupported(): Boolean = execute("git", "--version").isSuccess

    @Throws(IOException::class)
    override fun detectRepository(): Boolean =
            execute("git", "cat-file", "-e", headId).isSuccess &&
                    execute("git", "rev-parse", "--show-prefix").get().isBlank()

    /*@Throws(IOException::class)
    override fun getRevision(revisionId: String): Revision =
            execute("git", "rev-list", "-1", format, "$revisionId^{commit}")
                    .getOrNull()
                    ?.pairUpLines()
                    ?.singleOrNull()
                    ?.let(this::parseCommit)
                    ?: throw RevisionNotFoundException(revisionId)*/

    @Throws(IOException::class)
    override fun getRawRevision(revisionId: String): String =
            execute("git", "rev-list", "-1", format, "$revisionId^{commit}")
                    .getOrNull()
                    ?.lines()
                    ?.formatCommit()
                    ?: throw RevisionNotFoundException(revisionId)

    @Throws(IOException::class)
    override fun getHead(): Revision = try {
        getRevision(headId)
    } catch (e: RevisionNotFoundException) {
        throw IllegalStateException(e)
    }

    @Throws(IOException::class)
    override fun listFiles(): Set<String> =
            execute("git", "ls-tree", "--name-only", "-r", headId)
                    .get()
                    .lines()
                    .filter(String::isNotBlank)
                    .toSet()

    @Throws(IOException::class)
    override fun getFile(revisionId: String, path: String): String? {
        validateRevision(revisionId)
        return execute("git", "cat-file", "blob", "$revisionId:$path")
                .getOrNull()
    }

    @Throws(IOException::class)
    override fun getFileHistory(path: String): List<Revision> = execute(
            "git", "rev-list", "--first-parent", "--reverse", format,
            headId, "--", path
    ).get().pairUpLines()
            .map(this::parseCommit)
}
