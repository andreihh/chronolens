/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import java.io.IOException
import java.io.UncheckedIOException
import org.chronolens.api.database.RepositoryDatabase
import org.chronolens.api.repository.Repository
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.chronolens.model.SourceTree
import org.chronolens.model.SourceTreeEdit.Companion.apply

/**
 * A wrapper around a repository which has its interpreted history persisted in a database.
 *
 * All queries read the interpreted history directly from the database, not having to reinterpret it
 * again or to communicate with other subprocesses.
 */
internal class PersistentRepository(private val database: RepositoryDatabase) : Repository {
  override fun getHeadId(): RevisionId = listRevisions().last()

  override fun listSources(revisionId: RevisionId): Set<SourcePath> =
    getSnapshot(revisionId).sources.map(SourceFile::path).toSet()

  override fun listRevisions(): List<RevisionId> = runOrThrowUnchecked {
    val revisionIds = database.readHistoryIds()
    check(revisionIds.isNotEmpty()) { "Empty repository!" }
    revisionIds
  }

  override fun getSource(path: SourcePath, revisionId: RevisionId): SourceFile? =
    getSnapshot(revisionId)[path]

  override fun getSnapshot(revisionId: RevisionId): SourceTree {
    val snapshot = SourceTree.empty()
    for (revision in getHistory()) {
      snapshot.apply(revision.edits)
      if (revision.id == revisionId) return snapshot
    }
    throw IllegalArgumentException("Revision '$revisionId' doesn't exist!")
  }

  override fun getHistory(): Sequence<Revision> = runOrThrowUnchecked {
    database.readHistory().ifEmpty { error("History must not be empty!") }
  }

  override fun close() {
    runOrThrowUnchecked { database.close() }
  }
}

/**
 * Writes the history of [this] repository to the given [database] and reports the progress to the
 * given [listener].
 *
 * @throws IllegalStateException if the repository is corrupted or closed
 * @throws UncheckedIOException if any I/O errors occur
 */
public fun Repository.persist(
  database: RepositoryDatabase,
  listener: Repository.HistoryProgressListener? = null
): Repository = runOrThrowUnchecked {
  database.writeHistory(getHistory(listener))
  PersistentRepository(database)
}

private fun <T> runOrThrowUnchecked(block: () -> T): T =
  try {
    block()
  } catch (e: IOException) {
    throw UncheckedIOException(e)
  }
