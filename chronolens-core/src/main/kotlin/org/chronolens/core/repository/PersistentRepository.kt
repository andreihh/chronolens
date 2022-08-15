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
internal class PersistentRepository(private val database: RepositoryDatabase) :
    Repository {

    private val head by lazy {
        listRevisions().lastOrNull() ?: repositoryError("Repository history must not be empty!")
    }

    override fun getHeadId(): RevisionId = head

    override fun listSources(revisionId: RevisionId): Set<SourcePath> =
        getSnapshot(revisionId).sources.map(SourceFile::path).toSet()

    override fun listRevisions(): List<RevisionId> = getHistory().map(Revision::id).toList()

    override fun getSource(path: SourcePath, revisionId: RevisionId): SourceFile? =
        getSnapshot(revisionId)[path]

    override fun getSnapshot(revisionId: RevisionId): SourceTree {
        val snapshot = SourceTree.empty()
        for (revision in getHistory()) {
            snapshot.apply(revision.edits)
            if (revision.id == revisionId) break
        }
        return snapshot
    }

    override fun getHistory(): Sequence<Revision> = try {
        database.readHistory()
    } catch (e: IOException) {
        throw UncheckedIOException(e)
    }
}
