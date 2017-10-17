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

import org.metanalysis.core.logging.LoggerFactory
import org.metanalysis.core.model.SourceNode.SourceUnit
import org.metanalysis.core.model.Transaction
import org.metanalysis.core.serialization.JsonModule

import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * A wrapper around a repository which has all its interpreted data persisted on
 * disk.
 *
 * All queries read the interpreted data directly from disk, not having to
 * reinterpret it again or to communicate with other subprocesses.
 */
class PersistentRepository private constructor() : Repository {
    companion object {
        private val logger = LoggerFactory.getLogger<PersistentRepository>()

        private fun getRootDirectory() = File(".metanalysis")

        private fun getHeadFile() = File(getRootDirectory(), "HEAD")
        private fun getSourcesFile() = File(getRootDirectory(), "SOURCES")
        private fun getHistoryFile() = File(getRootDirectory(), "HISTORY")

        private fun getSnapshotDirectory(headId: String) =
                File(File(getRootDirectory(), "snapshots"), headId)

        private fun getSourceUnitDirectory(headId: String, path: String) =
                File(getSnapshotDirectory(headId), path)

        private fun getSourceUnitFile(headId: String, path: String) =
                File(getSourceUnitDirectory(headId, path), "model.json")

        private fun getTransactionsDirectory() =
                File(getRootDirectory(), "transactions")

        private fun getTransactionFile(id: String) =
                File(getTransactionsDirectory(), "$id.json")

        private fun Repository.persistSourceUnit(path: String) {
            val sourceUnit = getSourceUnit(path)
                    ?: error("'$path' couldn't be interpreted!")
            val headId = getHeadId()
            getSourceUnitDirectory(headId, path).mkdirs()
            getSourceUnitFile(headId, path).outputStream().use { out ->
                JsonModule.serialize(out, sourceUnit)
            }
        }

        private fun Repository.persistSnapshot() {
            logger.info("Persisting latest repository snapshot...\n")
            getSnapshotDirectory(getHeadId()).mkdirs()
            getSourcesFile().printWriter().use { out ->
                val sources = listSources()
                for ((index, path) in sources.withIndex()) {
                    out.println(path)
                    persistSourceUnit(path)
                    logger.info("Persisted ${index + 1} sources...\r")
                }
                logger.info("\n")
                logger.info("Done!\n")
            }
        }

        private fun persistTransaction(transaction: Transaction) {
            val file = getTransactionFile(transaction.id)
            file.outputStream().use { out ->
                JsonModule.serialize(out, transaction)
            }
        }

        private fun Repository.persistHistory() {
            logger.info("Persisting repository history...\n")
            getHistoryFile().printWriter().use { out ->
                getTransactionsDirectory().mkdirs()
                for ((index, transaction) in getHistory().withIndex()) {
                    out.println(transaction.id)
                    persistTransaction(transaction)
                    logger.info("Persisted ${index + 1} transactions...\r")
                }
                logger.info("\n")
                logger.info("Done!\n")
            }
        }

        /**
         * Returns the instance which can query the repository detected in the
         * current working directory for code metadata.
         *
         * @return the repository instance, or `null` if no repository was
         * detected
         * @throws IOException if any input related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic
        fun load(): PersistentRepository? =
                if (getRootDirectory().exists()) PersistentRepository()
                else null

        /**
         * Persists this repository in the current working directory.
         *
         * @return the persisted repository
         * @throws IOException if any output related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic
        fun Repository.persist(): PersistentRepository {
            if (this is PersistentRepository) {
                return this
            }
            getRootDirectory().mkdirs()
            getHeadFile().printWriter().use { out -> out.println(getHeadId()) }
            persistSnapshot()
            persistHistory()
            return PersistentRepository()
        }

        /**
         * Deletes the previously persisted repository from the current working
         * directory.
         *
         * @throws IOException if any input or output related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic
        fun clean() {
            getRootDirectory().deleteRecursively()
        }
    }

    private val headId = getHeadFile().readLines().first()
    private val sources =
            getSourcesFile().readLines().filter(String::isNotBlank).toSet()
    private val history =
            getHistoryFile().readLines().filter(String::isNotBlank)

    private inline fun <T> File.use(block: (src: InputStream) -> T): T = try {
        inputStream().use(block)
    } catch (e: IOException) {
        throw IllegalStateException(e)
    }

    override fun getHeadId(): String = headId

    override fun listSources(): Set<String> = sources

    override fun getSourceUnit(path: String): SourceUnit? {
        val file = getSourceUnitFile(headId, path)
        return if (!file.exists()) null
        else file.use(JsonModule::deserialize)
    }

    override fun getHistory(): Iterable<Transaction> =
            history.asSequence().map { id ->
                val file = getTransactionFile(id)
                file.use { src -> JsonModule.deserialize<Transaction>(src) }
            }.asIterable()
}
