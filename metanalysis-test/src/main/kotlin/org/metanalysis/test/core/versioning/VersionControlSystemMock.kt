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

package org.metanalysis.test.core.versioning

import org.metanalysis.core.versioning.Commit
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.InputStream

class VersionControlSystemMock(
        private val commits: List<CommitMock>
) : VersionControlSystem() {
    companion object {
        val NAME: String = "mockfs"
    }

    data class CommitMock(
            val id: String,
            val author: String,
            val date: String,
            val changedFiles: Map<String, InputStream?>
    )

    private val files: Map<String, Map<String, InputStream>>
    private val commitsById = commits.associate { (id, author, date, _) ->
        id to Commit(id, author, date)
    }

    init {
        val fileHistory = hashMapOf<String, Map<String, InputStream>>()
        val currentFiles = hashMapOf<String, InputStream>()
        commits.forEach { (id, _, _, changedFiles) ->
            changedFiles.forEach { path, src ->
                if (src == null) {
                    currentFiles -= path
                } else {
                    currentFiles[path] = src
                }
            }
            fileHistory[id] = currentFiles.toMap()
        }
        files = fileHistory
    }

    override val name: String
        get() = NAME

    override fun getHead(): String = commits.last().id

    override fun listFiles(commitId: String): Set<String> =
            requireNotNull(files[commitId]).keys

    override fun getCommit(commitId: String): Commit =
            requireNotNull(commitsById[commitId])

    override fun getFile(path: String, commitId: String): InputStream? =
            requireNotNull(files[commitId])[path]

    override fun getFileHistory(path: String, commitId: String): List<String> =
            commits.filter { path in it.changedFiles }.map(CommitMock::id)
}
