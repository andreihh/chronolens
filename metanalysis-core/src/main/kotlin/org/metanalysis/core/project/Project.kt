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
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.File
import java.io.IOException

/**
 * @throws IllegalStateException if the given version control system is not
 * supported or if the repository is in an invalid state
 */
class Project(vcs: String) {
    data class HistoryEntry(
            val commit: String,
            val author: String,
            val date: String,
            val transaction: SourceFileTransaction?
    )

    private val vcs = checkNotNull(VersionControlSystem.getByName(vcs)) {
        "'$vcs' is not supported!"
    }
    private val head = this.vcs.getHead()

    /**
     * @throws IllegalArgumentException if the given `commit` is invalid
     * @throws IOException if any input-related errors occur or if the given
     * file contains invalid code at any point in time or if no provided parsers
     * can interpret the given file
     */
    @Throws(IOException::class)
    fun getFileHistory(
            path: String,
            commit: String = head
    ): List<HistoryEntry> {
        val history = arrayListOf<HistoryEntry>()
        var sourceFile = SourceFile()
        vcs.getFileHistory(path, commit).forEach { commitId ->
            val (_, author, date) = vcs.getCommit(commitId)
            val newSourceFile = getFileModel(path, commitId) ?: SourceFile()
            val transaction = sourceFile.diff(newSourceFile)
            history += HistoryEntry(commitId, author, date, transaction)
            sourceFile = newSourceFile
        }
        return history
    }

    /**
     * @return `null` if the given `path` doesn't exist in the given commit
     * @throws IllegalArgumentException if the given `commit` is invalid
     * @throws IOException if any input-related errors occur or if the given
     * file contains invalid code or if no provided parsers can interpret the
     * given file
     */
    @Throws(IOException::class)
    fun getFileModel(path: String, commit: String = head): SourceFile? {
        val file = vcs.getFile(path, commit) ?: return null
        val parser = Parser.getByExtension(File(path).extension)
                ?: throw IOException("No parser can interpret '$path'!")
        return parser.parse(file)
    }

    /**
     * @throws IllegalArgumentException if the given `srcCommit` or `dstCommit`
     * are invalid or if the file at the given `path` doesn't exist in either
     * commits
     * @throws IOException if any input-related errors occur or if the given
     * file contains invalid code or if no provided parsers can interpret the
     * given file
     */
    @Throws(IOException::class)
    fun getFileDiff(
            path: String,
            srcCommit: String,
            dstCommit: String = head
    ): SourceFileTransaction? {
        val srcSourceFile = getFileModel(path, srcCommit)
        val dstSourceFile = getFileModel(path, dstCommit)
        require(srcSourceFile != null || dstSourceFile != null) {
            "File '$path' doesn't exist in '$srcCommit' or '$dstCommit'!"
        }
        return (srcSourceFile ?: SourceFile())
                .diff(dstSourceFile ?: SourceFile())
    }

    /**
     * @throws IllegalArgumentException if the given `commit` is invalid
     */
    fun listFiles(commit: String = head): Set<String> = vcs.listFiles(commit)
}
