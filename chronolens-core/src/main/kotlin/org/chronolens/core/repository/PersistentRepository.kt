/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.repository

import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.SourceTreeEdit.Companion.apply
import java.io.IOException
import java.io.UncheckedIOException

/**
 * A wrapper around a repository which has its interpreted history persisted on disk.
 *
 * All queries read the interpreted history directly from disk, not having to reinterpret it again
 * or to communicate with other subprocesses.
 */
internal class PersistentRepository(private val storage: RepositoryStorage) : Repository {
    private val history by lazy {
        try {
            storage.readHistoryIds().checkValidHistory()
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    private val head by lazy { history.last() }

    override fun getHeadId(): RevisionId = head

    override fun listSources(revisionId: RevisionId): Set<SourcePath> =
        getSnapshot(revisionId).sources.map(SourceFile::path).toSet()

    override fun listRevisions(): List<RevisionId> = history

    override fun getSource(path: SourcePath, revisionId: RevisionId): SourceFile? =
        getSnapshot(revisionId)[path]

    override fun getSnapshot(revisionId: RevisionId): SourceTree {
        require(revisionId in history) { "Revision '$revisionId' doesn't exist!" }
        val snapshot = SourceTree.empty()
        for (revision in getHistory()) {
            snapshot.apply(revision.edits)
            if (revision.id == revisionId) return snapshot
        }
        repositoryError("Revision '$revisionId' should exist but not found in history!")
    }

    override fun getHistory(): Sequence<Revision> =
        try {
            storage.readHistory().ifEmpty { repositoryError("History must not be empty!") }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
}
