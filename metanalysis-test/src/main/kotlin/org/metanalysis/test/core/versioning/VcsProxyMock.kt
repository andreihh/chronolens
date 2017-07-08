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

import org.metanalysis.core.versioning.Revision
import org.metanalysis.core.versioning.RevisionNotFoundException
import org.metanalysis.core.versioning.VcsProxy

class VcsProxyMock(private val commits: List<CommitMock>) : VcsProxy {
    private val commitsById = commits.associateBy(CommitMock::id)
    private val files: Map<String, Map<String, String>>

    init {
        val fileHistory = hashMapOf<String, Map<String, String>>()
        val currentFiles = hashMapOf<String, String>()
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

    private fun CommitMock.toRevision(): Revision = Revision(id, date, author)

    private fun getCommit(id: String): CommitMock =
            commitsById[id] ?: throw RevisionNotFoundException(id)

    override fun getHead(): Revision = commits.lastOrNull()?.toRevision()
            ?: throw RevisionNotFoundException("HEAD")

    override fun listFiles(): Set<String> =
            checkNotNull(files[getHead().id]).keys

    @Throws(RevisionNotFoundException::class)
    override fun getRevision(revisionId: String): Revision =
            getCommit(revisionId).toRevision()

    @Throws(RevisionNotFoundException::class)
    override fun getFile(revisionId: String, path: String): String? =
            checkNotNull(files[getCommit(revisionId).id])[path]

    override fun getFileHistory(path: String): List<Revision> =
            commits.filter { path in it.changedFiles }
                    .map { it.toRevision() }
}
