/*
 * Copyright 2018-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.SourceFile
import org.chronolens.core.serialization.JsonModule
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.util.Collections.unmodifiableList
import java.util.Collections.unmodifiableSet

/**
 * A wrapper around a repository which has all its interpreted data persisted on
 * disk.
 *
 * All queries read the interpreted data directly from disk, not having to
 * reinterpret it again or to communicate with other subprocesses.
 */
public class PersistentRepository
private constructor(private val schema: RepositoryFileSchema) : Repository {

    private val head by lazy {
        val rawHeadId = schema.headFile.readFileLines()
        checkState(rawHeadId.size == 1) {
            "'${schema.headFile}' must contain a single line with the head id!"
        }
        checkValidRevisionId(rawHeadId.single())
    }

    private val sources by lazy {
        val rawSources = schema.sourcesFile.readFileLines()
        checkValidSources(rawSources)
    }

    private val history by lazy {
        val rawHistory = schema.historyFile.readFileLines()
        checkValidHistory(rawHistory)
    }

    override fun getHeadId(): String = head

    override fun listSources(): Set<String> = unmodifiableSet(sources)

    override fun listRevisions(): List<String> = unmodifiableList(history)

    override fun getSource(path: String): SourceFile? {
        validatePath(path)
        val file = schema.getSourceFile(path)
        return if (path in sources) JsonModule.deserialize(file) else null
    }

    override fun getHistory(): Sequence<Transaction> =
        history.asSequence().map { transactionId ->
            val file = schema.getTransactionFile(transactionId)
            try {
                JsonModule.deserialize(file)
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }

    public companion object {
        /**
         * Returns the persisted repository detected in the given [directory],
         * or `null` if no repository was detected.
         *
         * @throws IOException if any input related errors occur
         * @throws CorruptedRepositoryException if the repository is corrupted
         */
        @Throws(IOException::class)
        @JvmStatic
        public fun load(directory: File): PersistentRepository? {
            val schema = RepositoryFileSchema(directory)
            return if (!schema.rootDirectory.isDirectory) null
            else PersistentRepository(schema)
        }

        /**
         * Persists [this] repository in the given [directory], notifying the
         * given [listener] of the progress if it is not `null`, and returns the
         * persisted repository.
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
         * Deletes the previously persisted repository from the given
         * [directory].
         *
         * All corresponding [PersistentRepository] instances will become
         * corrupted after this method is called.
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
        public fun onSnapshotStart(headId: String, sourceCount: Int)
        public fun onSourcePersisted(path: String)
        public fun onSnapshotEnd()
        public fun onHistoryStart(revisionCount: Int)
        public fun onTransactionPersisted(id: String)
        public fun onHistoryEnd()
    }
}
