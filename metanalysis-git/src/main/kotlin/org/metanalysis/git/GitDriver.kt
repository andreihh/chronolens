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

import org.metanalysis.core.versioning.RevisionNotFoundException
import org.metanalysis.core.versioning.Subprocess.execute
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.FileNotFoundException
import java.io.IOException

/** A module which integrates the `git` version control system. */
class GitDriver : VersionControlSystem() {
    private fun String.spread(): Array<String> = split(' ').toTypedArray()

    private fun List<String>.formatCommit(): String? =
            if (size < 2) null
            else "${get(0)}:${get(1)}".removePrefix("commit ")

    @Throws(IOException::class)
    override fun isSupported(): Boolean = execute("git", "--version").isSuccess

    @Throws(IOException::class)
    override fun detectRepository(): Boolean =
            execute("git", "cat-file", "-e", "HEAD").isSuccess

    @Throws(IOException::class)
    override fun getRawRevision(revisionId: String): String = execute(
            *"git rev-list -1 --format=%at:%an".spread(),
            "$revisionId^{commit}"
    ).getOrNull()
            ?.lines()
            ?.formatCommit()
            ?: throw RevisionNotFoundException(revisionId)

    @Throws(IOException::class)
    override fun getHead(): Revision = try {
        getRevision("HEAD")
    } catch (e: RevisionNotFoundException) {
        throw IllegalStateException(e)
    }

    @Throws(IOException::class)
    override fun listFiles(revision: Revision): Set<String> {
        validateRevision(revision)
        return execute("git", "ls-tree", "--name-only", "-r", revision.id)
                .get().lines()
                .filter(String::isNotBlank)
                .toSet()
    }

    @Throws(IOException::class)
    override fun getFile(revision: Revision, path: String): String? {
        validateRevision(revision)
        return execute("git", "cat-file", "blob", "${revision.id}:$path")
                .getOrNull()
    }

    @Throws(IOException::class)
    override fun getFileHistory(
            revision: Revision,
            path: String
    ): List<Revision> {
        validateRevision(revision)
        return execute(
                *"git rev-list --first-parent --reverse".spread(),
                revision.id,
                "--",
                path
        ).get().lines()
                .filter(String::isNotBlank)
                .map(this::getRevision)
                .takeIf(List<Revision>::isNotEmpty)
                ?: throw FileNotFoundException(
                        "'$path' doesn't exist in '${revision.id}'!"
                )
    }
}
