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
import org.metanalysis.core.versioning.VcsProxy

class VcsProxyMock(private val revisions: List<RevisionMock>) : VcsProxy {
    private val revisionsById = revisions.associateBy(RevisionMock::id)
    private val files: Map<String, Map<String, String?>>

    init {
        val fileHistory = hashMapOf<String, Map<String, String?>>()
        val currentFiles = hashMapOf<String, String?>()
        val ids = hashSetOf<String>()
        for ((id, _, _, changeSet) in revisions) {
            require(id !in ids) { "Duplicated revision id '$id'!" }
            currentFiles += changeSet
            fileHistory[id] = currentFiles.filterValues { it != null }
            ids += id
        }
        files = fileHistory
    }

    private fun RevisionMock.toRevision(): Revision = Revision(id, date, author)

    private fun getRevisionMock(id: String): RevisionMock =
            requireNotNull(revisionsById[id]) {
                "Revision '$id' doesn't exist!"
            }

    override fun getHead(): Revision =
            checkNotNull(revisions.lastOrNull()).toRevision()

    override fun listFiles(revisionId: String): Set<String> =
            checkNotNull(files[getRevisionMock(revisionId).id]).keys

    override fun getRevision(revisionId: String): Revision =
            getRevisionMock(revisionId).toRevision()

    override fun getChangeSet(revisionId: String): Set<String> =
            getRevisionMock(revisionId).changeSet.keys

    override fun getFile(revisionId: String, path: String): String? =
            checkNotNull(files[getRevisionMock(revisionId).id])[path]

    override fun getHistory(path: String): List<Revision> =
            revisions.filter { commit ->
                commit.changeSet.keys.any { file -> file.startsWith(path) }
            }.map { it.toRevision() }
}
