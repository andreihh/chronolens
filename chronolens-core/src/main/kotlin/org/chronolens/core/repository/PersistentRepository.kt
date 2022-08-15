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

import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.util.Collections.unmodifiableList
import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.SourceTreeEdit.Companion.apply
import org.chronolens.core.serialization.JsonModule

/**
 * A wrapper around a repository which has all its interpreted data persisted on disk.
 *
 * All queries read the interpreted data directly from disk, not having to reinterpret it again or
 * to communicate with other subprocesses.
 */
public class PersistentRepository internal constructor(private val schema: RepositoryFileSchema) :
    Repository {
    // TODO: wrap all IOExceptions into UncheckedIOExceptions.

    private val history by lazy {
        val rawHistory = schema.historyFile.readFileLines()
        checkValidHistory(rawHistory)
    }

    private val head by lazy { history.last() }

    override fun getHeadId(): RevisionId = head

    override fun listSources(revisionId: RevisionId): Set<SourcePath> =
        getSnapshot(revisionId).sources.map(SourceFile::path).toSet()

    override fun listRevisions(): List<RevisionId> = unmodifiableList(history)

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

    override fun getHistory(): Sequence<Revision> =
        history.asSequence().map { revisionId ->
            val file = schema.getRevisionsFile(revisionId)
            try {
                JsonModule.deserialize(file)
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }

    public companion object {
        /**
         * Persists [this] repository in the given [directory], notifying the given [listener] of
         * the progress if it is not `null`, and returns the persisted repository.
         *
         * @throws IOException if any output related errors occur
         * @throws CorruptedRepositoryException if the repository is corrupted
         */
        @Throws(IOException::class)
        @JvmStatic
        public fun Repository.persist(
            directory: File,
            listener: ProgressListener? = null,
        ): PersistentRepository {
            val schema = RepositoryFileSchema(directory)
            if (this is PersistentRepository && schema == this.schema) {
                return this
            }
            RepositoryPersister(this, schema, listener).persist()
            return PersistentRepository(schema)
        }

        /**
         * Deletes the previously persisted repository from the given [directory].
         *
         * All corresponding [PersistentRepository] instances will become corrupted after this
         * method is called.
         *
         * @throws IOException if any input or output related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic
        public fun clean(directory: File) {
            val schema = RepositoryFileSchema(directory)
            schema.rootDirectory.deleteRecursively()
        }
    }

    /** A listener notified on the progress of persisting a repository. */
    public interface ProgressListener {
        public fun onHistoryStart(revisionCount: Int)
        public fun onRevisionPersisted(revisionId: RevisionId)
        public fun onHistoryEnd()
    }
}
