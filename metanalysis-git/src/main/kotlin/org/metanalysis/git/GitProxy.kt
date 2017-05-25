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
import org.metanalysis.core.versioning.Revision
import org.metanalysis.core.versioning.RevisionNotFoundException
import org.metanalysis.core.versioning.VcsProxy

import java.io.IOException
import java.util.Date

/** A module which integrates the `git` version control system. */
class GitProxy : VcsProxy() {
    private val vcs = "git"
    private val headId = "HEAD"
    private val format = "--format=%at:%an"

    private fun validateRevision(revisionId: String) {
        val result = execute(vcs, "cat-file", "-e", "$revisionId^{commit}")
        if (!result.isSuccess) {
            throw RevisionNotFoundException(revisionId)
        }
    }

    private fun String.formatCommits(): List<String> {
        val lines = lines()
        return (0 until lines.size - 1 step 2).map { i ->
            "${lines[i]}:${lines[i + 1]}".removePrefix("commit ")
        }
    }

    private fun String.formatCommit(): String? = formatCommits().singleOrNull()

    private fun parseCommit(formattedLine: String): Revision {
        val (id, rawDate, author) = formattedLine.split(':', limit = 3)
        val date = Date(rawDate.toLong() * 1000)
        return Revision(id, date, author)
    }

    @Throws(IOException::class)
    override fun isSupported(): Boolean = execute(vcs, "--version").isSuccess

    @Throws(IOException::class)
    override fun detectRepository(): Boolean =
            execute(vcs, "rev-parse", "--show-prefix").getOrNull()?.isBlank()
                    ?: false

    @Throws(IOException::class)
    override fun getHead(): Revision = getRevision(headId)

    @Throws(IOException::class)
    override fun getRevision(revisionId: String): Revision =
            execute(vcs, "rev-list", "-1", format, "$revisionId^{commit}")
                    .getOrNull()
                    ?.formatCommit()
                    ?.let(this::parseCommit)
                    ?: throw RevisionNotFoundException(revisionId)


    @Throws(IOException::class)
    override fun listFiles(): Set<String> =
            execute(vcs, "ls-tree", "--full-tree", "--name-only", "-r", headId)
                    .get()
                    .lines()
                    .filter(String::isNotBlank)
                    .toSet()

    @Throws(IOException::class)
    override fun getFile(revisionId: String, path: String): String? {
        validateRevision(revisionId)
        return execute(vcs, "cat-file", "blob", "$revisionId:$path").getOrNull()
    }

    @Throws(IOException::class)
    override fun getFileHistory(path: String): List<Revision> = execute(
            vcs, "rev-list", "--first-parent", "--reverse", format,
            headId, "--", path
    ).get().formatCommits().map(this::parseCommit)
}
