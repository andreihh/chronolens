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

package org.metanalysis.core.project

import org.metanalysis.core.delta.SourceFileTransaction
import org.metanalysis.core.delta.SourceFileTransaction.Companion.diff
import org.metanalysis.core.model.Parser
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.versioning.VersionControlSystem.Companion.get
import org.metanalysis.core.versioning.VersionControlSystem.Companion.getByName

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Date

/**
 * @throws IllegalStateException if the given version control system is not
 * supported or if the repository is in an invalid state
 */
class Project(vcs: String? = null) {
    data class HistoryEntry(
            val commit: String,
            val author: String,
            val date: Date,
            val transaction: SourceFileTransaction?
    )

    data class History(
            private val entries: List<HistoryEntry>
    ) : List<HistoryEntry> by entries

    private val vcs = if (vcs != null)
        checkNotNull(vcs.let(::getByName)) { "'$vcs' is not supported!" }
    else checkNotNull(get()) { "VCS root could not be unambiguously detected!" }
    private val head = this.vcs.getHead().id

    /**
     * @throws IllegalArgumentException if the given `revision` is invalid
     * @throws IOException if any input related errors occur or if the given
     * file contains invalid code at any point in time or if no provided parsers
     * can interpret the given file
     */
    @Throws(IOException::class)
    fun getFileHistory(path: String, revision: String = head): History {
        val history = arrayListOf<HistoryEntry>()
        var sourceFile = SourceFile()
        vcs.getFileHistory(revision, path).forEach { (id, author, date) ->
            val newSourceFile = getFileModel(path, id) ?: SourceFile()
            val transaction = sourceFile.diff(newSourceFile)
            history += HistoryEntry(id, author, date, transaction)
            sourceFile = newSourceFile
        }
        return History(history)
    }

    /**
     * @return `null` if the given `path` doesn't exist in the given revision
     * @throws IllegalArgumentException if the given `revision` is invalid
     * @throws IOException if any input related errors occur or if the given
     * file contains invalid code or if no provided parsers can interpret the
     * given file
     */
    @Throws(IOException::class)
    fun getFileModel(path: String, revision: String = head): SourceFile? {
        val source = try {
            vcs.getFile(revision, path)
        } catch (e: FileNotFoundException) {
            return null
        }
        val parser = Parser.getByExtension(File(path).extension)
                ?: throw IOException("No parser can interpret '$path'!")
        return parser.parse(source)
    }

    /**
     * @throws IllegalArgumentException if the given `srcRevision` or
     * `dstRevision` are invalid or if the file at the given `path` doesn't
     * exist in either revisions
     * @throws IOException if any input related errors occur or if the given
     * file contains invalid code or if no provided parsers can interpret the
     * given file
     */
    @Throws(IOException::class)
    fun getFileDiff(
            path: String,
            srcRevision: String,
            dstRevision: String = head
    ): SourceFileTransaction? {
        val srcSourceFile = getFileModel(path, srcRevision)
        val dstSourceFile = getFileModel(path, dstRevision)
        require(srcSourceFile != null || dstSourceFile != null) {
            "File '$path' doesn't exist in '$srcRevision' or '$dstRevision'!"
        }
        return (srcSourceFile ?: SourceFile())
                .diff(dstSourceFile ?: SourceFile())
    }

    /**
     * @throws IllegalArgumentException if the given `revision` is invalid
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun listFiles(revision: String = head): Set<String> =
            vcs.listFiles(revision)
}
