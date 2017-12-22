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

package org.metanalysis.core.repository

import org.metanalysis.core.model.SourceNode.SourceUnit
import org.metanalysis.core.serialization.JsonModule
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.Collections.unmodifiableSet

/**
 * A wrapper around a repository which has all its interpreted data persisted on
 * disk.
 *
 * All queries read the interpreted data directly from disk, not having to
 * reinterpret it again or to communicate with other subprocesses.
 */
class PersistentRepository private constructor() : Repository {
    companion object {
        /**
         * Returns the instance which can query the repository detected in the
         * current working directory for code metadata.
         *
         * @return the repository instance, or `null` if no repository was
         * detected
         * @throws IOException if any input related errors occur
         * @throws IllegalStateException if the repository state is corrupted
         */
        @Throws(IOException::class)
        @JvmStatic
        fun load(): PersistentRepository? =
                if (rootDirectory.exists()) PersistentRepository()
                else null

        /**
         * Persists this repository in the current working directory.
         *
         * @param listener the listener which will be notified on the
         * persistence progress, or `null` if no notification is required
         * @return the persisted repository
         * @throws IOException if any output related errors occur
         * @throws IllegalStateException if the repository state is corrupted
         */
        @Throws(IOException::class)
        @JvmStatic
        fun Repository.persist(
                listener: ProgressListener? = null
        ): PersistentRepository {
            if (this is PersistentRepository) {
                return this
            }
            rootDirectory.mkdir()
            headFile.printWriter().use { out -> out.println(getHeadId()) }
            persistSnapshot(listener)
            persistHistory(listener)
            return PersistentRepository()
        }

        /**
         * Deletes the previously persisted repository from the current working
         * directory.
         *
         * All `PersistentRepository` instances will become corrupted after this
         * method is called.
         *
         * @throws IOException if any input or output related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic
        fun clean() {
            rootDirectory.deleteRecursively()
        }

        @JvmStatic
        private inline fun <T> File.use(block: (InputStream) -> T): T = try {
            inputStream().use(block)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }

        private val rootDirectory = File(".metanalysis")
        private val headFile = File(rootDirectory, "HEAD")
        private val sourcesFile = File(rootDirectory, "SOURCES")
        private val historyFile = File(rootDirectory, "HISTORY")
        private val snapshotDirectory = File(rootDirectory, "snapshot")
        private val transactionsDirectory =
                File(rootDirectory, "transactions")

        private fun getSourceUnitDirectory(path: String): File =
                File(snapshotDirectory, path)

        private fun getSourceUnitFile(path: String): File =
                File(getSourceUnitDirectory(path), "model.json")

        private fun getTransactionFile(id: String): File =
                File(transactionsDirectory, "$id.json")

        private fun persistSourceUnit(sourceUnit: SourceUnit) {
            getSourceUnitDirectory(sourceUnit.path).mkdirs()
            getSourceUnitFile(sourceUnit.path).outputStream().use { out ->
                JsonModule.serialize(out, sourceUnit)
            }
        }

        private fun Repository.persistSnapshot(listener: ProgressListener?) {
            listener?.onSnapshotStart(getHeadId())
            snapshotDirectory.mkdir()
            sourcesFile.printWriter().use { out ->
                for (path in listSources()) {
                    val sourceUnit = getSourceUnit(path)
                            ?: error("'$path' couldn't be interpreted!")
                    out.println(path)
                    persistSourceUnit(sourceUnit)
                    listener?.onSourcePersisted(path)
                }
            }
            listener?.onSnapshotEnd()
        }

        private fun persistTransaction(transaction: Transaction) {
            val file = getTransactionFile(transaction.id)
            file.outputStream().use { out ->
                JsonModule.serialize(out, transaction)
            }
        }

        private fun Repository.persistHistory(listener: ProgressListener?) {
            listener?.onHistoryStart()
            transactionsDirectory.mkdir()
            historyFile.printWriter().use { out ->
                for (transaction in getHistory()) {
                    out.println(transaction.id)
                    persistTransaction(transaction)
                    listener?.onTransactionPersisted(transaction.id)
                }
            }
            listener?.onHistoryEnd()
        }
    }

    /** A listener notified on the progress of persisting a repository. */
    interface ProgressListener {
        fun onSnapshotStart(headId: String)
        fun onSourcePersisted(path: String)
        fun onSnapshotEnd()
        fun onHistoryStart()
        fun onTransactionPersisted(id: String)
        fun onHistoryEnd()
    }

    private val headId = headFile.readLines().first()
    private val sources =
            sourcesFile.readLines().takeWhile(String::isNotEmpty).toSet()
    private val history = historyFile.readLines().takeWhile(String::isNotEmpty)

    init {
        checkValidTransactionId(headId)
        sources.forEach(::checkValidPath)
        checkValidHistory(history)
    }

    override fun getHeadId(): String = headId

    override fun listSources(): Set<String> = unmodifiableSet(sources)

    override fun getSourceUnit(path: String): SourceUnit? {
        validatePath(path)
        return if (path !in sources) null
        else getSourceUnitFile(path).use(JsonModule::deserialize)
    }

    override fun getHistory(): Iterable<Transaction> =
            history.mapLazy { transactionId ->
                val file = getTransactionFile(transactionId)
                file.use { src -> JsonModule.deserialize<Transaction>(src) }
            }
}
