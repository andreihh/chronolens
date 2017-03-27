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
import org.metanalysis.core.versioning.Branch
import org.metanalysis.core.versioning.Commit
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.File
import java.io.IOException

class Project(vcs: String) {
    data class HistoryEntry(
            val commitId: String,
            val author: String,
            val date: String,
            val transaction: SourceFileTransaction?
    )

    private val vcs = checkNotNull(VersionControlSystem.getByName(vcs))
    private val headId = this.vcs.getHead().id
    private val currentBranchName = this.vcs.getCurrentBranch().name

    private fun getCommit(id: String): Commit =
            requireNotNull(vcs.getCommit(id)) { "Invalid commit $id!" }

    private fun getBranch(name: String): Branch =
            requireNotNull(vcs.getBranch(name)) { "Invalid branch $name!" }

    /**
     * @throws IllegalArgumentException if the given `branch` doesn't exist or
     * if the file at the given `path` doesn't exist in the given `branch`
     */
    fun getFileHistory(
            path: String,
            branchName: String = currentBranchName
    ): List<HistoryEntry> {
        val branch = getBranch(branchName)
        val history = arrayListOf<HistoryEntry>()
        var sourceFile = SourceFile()
        vcs.getFileHistory(path, branch).forEach { (commitId, author, date) ->
            val newSourceFile = getFileModel(path, commitId) ?: SourceFile()
            val transaction = sourceFile.diff(newSourceFile)
            history += HistoryEntry(commitId, author, date, transaction)
            sourceFile = newSourceFile
        }
        require(history.isNotEmpty()) {
            "File $path doesn't exist in branch $branchName!"
        }
        return history
    }

    /**
     * @return `null` if the given `path` doesn't exist in the given commit
     * @throws IllegalArgumentException if the given `commitId` doesn't exist
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getFileModel(path: String, commitId: String = headId): SourceFile? {
        val file = vcs.getFile(path, getCommit(commitId)) ?: return null
        val parser = Parser.getByExtension(File(path).extension)
                ?: throw IOException("Provided parsers can't interpret $path!")
        return parser.parse(file)
    }

    /**
     * @throws IllegalArgumentException if the given `srcCommitId` or
     * `dstCommitId` don't exist or if the file at the given `path` doesn't
     * exist in either commits
     */
    @Throws(IOException::class)
    fun getFileDiff(
            path: String,
            srcCommitId: String,
            dstCommitId: String = headId
    ): SourceFileTransaction? {
        val srcSourceFile = getFileModel(path, srcCommitId)
        val dstSourceFile = getFileModel(path, dstCommitId)
        require(srcSourceFile != null || dstSourceFile != null) {
            "File $path doesn't exist in $srcCommitId or $dstCommitId!"
        }
        return (srcSourceFile ?: SourceFile())
                .diff(dstSourceFile ?: SourceFile())
    }

    /**
     * @throws IllegalArgumentException if the given `commitId` doesn't exist
     */
    fun getFiles(commitId: String): Set<String> =
            vcs.getFiles(getCommit(commitId))
}
