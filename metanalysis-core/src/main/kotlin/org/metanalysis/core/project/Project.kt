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
class Project @Throws(IOException::class) constructor(vcs: String? = null) {
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

    init {
        check(this.vcs.detectRepository()) {
            "No '${this.vcs.name}' repository detected!"
        }
    }

    private val head = this.vcs.getHead().id

    /**
     * @throws IOException if the given `revision` is invalid
     * @throws IOException if the given `revision` doesn't exist or if the given
     * `path` never existed in the given `revision` or any of its ancestors or
     * if the given `path` contained invalid code at any point in time or if
     * none of the provided parsers can interpret the file at the given `path`
     * or if any input related errors occur
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
     * @param path the relative path of the file which should be interpreted
     * @return the parsed code metadata, or `null` if the given `path` doesn't
     * exist in the given `revision`
     * @throws IOException if the given `revision` doesn't exist or the given
     * `path` contains invalid code or if none of the provided parsers can
     * interpret the file at the given `path` or if any input related errors
     * occur
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
     * @throws IOException if the given `srcRevision` or `dstRevision` don't
     * exist or if the given `path` doesn't exist in either revisions or if
     * the given `path` contained invalid code in either revisions or if none of
     * the provided parsers can interpreted the file at the given `path` or if
     * any input related errors occur
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
     * @throws IOException if the given `revision` doesn't exist or if any input
     * related errors occur
     */
    @Throws(IOException::class)
    fun listFiles(revision: String = head): Set<String> =
            vcs.listFiles(revision)
}
