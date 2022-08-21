/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.core.repository

import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.SourceTreeEdit.Companion.apply
import org.chronolens.core.repository.CorruptedRepositoryException
import org.chronolens.core.repository.Repository

internal class FakeRepository(revisions: List<Revision>) : Repository {
    private val history by lazy {
        if (revisions.isEmpty()) {
            throw CorruptedRepositoryException("History must not be empty!")
        }
        revisions
    }

    private val snapshots by lazy {
        val snapshots = mutableMapOf<RevisionId, SourceTree>()
        val snapshot = SourceTree.empty()
        for (revision in history) {
            snapshot.apply(revision.edits)
            snapshots[revision.id] = SourceTree.of(snapshot.sources)
        }
        snapshots
    }

    override fun getHeadId(): RevisionId = history.last().id

    override fun listSources(revisionId: RevisionId): Set<SourcePath> =
        requireNotNull(snapshots[revisionId]).sources.map(SourceFile::path).toSet()

    override fun listRevisions(): List<RevisionId> = history.map(Revision::id)

    override fun getSource(path: SourcePath, revisionId: RevisionId): SourceFile? =
        requireNotNull(snapshots[revisionId])[path]

    override fun getSnapshot(revisionId: RevisionId): SourceTree =
        requireNotNull(snapshots[revisionId])

    override fun getHistory(): Sequence<Revision> = history.asSequence()
}
