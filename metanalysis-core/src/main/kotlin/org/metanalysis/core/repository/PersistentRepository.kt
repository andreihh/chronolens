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
                if (rootDirectory.exists()) PersistentRepository()
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
            rootDirectory.mkdirs()
            headFile.printWriter().use { out -> out.println(getHeadId()) }
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
            rootDirectory.deleteRecursively()
        }
    }

    private val headId = headFile.readLines().first()
    private val sources =
            sourcesFile.readLines().filter(String::isNotBlank).toSet()
    private val history = historyFile.readLines().filter(String::isNotBlank)

    init {
        for (path in sources) {
            check(getSourceUnitFile(path).exists()) { "'$path' doesn't exist!" }
        }
        for (id in history) {
            check(getTransactionFile(id).exists()) { "'$id' doesn't exist!" }
        }
    }

    override fun getHeadId(): String = headId

    override fun listSources(): Set<String> = sources

    override fun getSourceUnit(path: String): SourceUnit? =
            if (path !in sources) null
            else getSourceUnitFile(path).use(JsonModule::deserialize)

    override fun getHistory(): Iterable<Transaction> =
            history.asSequence().map(::getTransactionFile).map { file ->
                file.use { src -> JsonModule.deserialize<Transaction>(src) }
            }.asIterable()
}

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
private val transactionsDirectory = File(rootDirectory, "transactions")

private fun getSourceUnitDirectory(path: String): File =
        File(snapshotDirectory, path)

private fun getSourceUnitFile(path: String): File =
        File(getSourceUnitDirectory(path), "model.json")

private fun getTransactionFile(id: String): File =
        File(transactionsDirectory, "$id.json")

private fun Repository.persistSourceUnit(path: String) {
    val sourceUnit = getSourceUnit(path)
            ?: error("'$path' couldn't be interpreted!")
    getSourceUnitDirectory(path).mkdirs()
    getSourceUnitFile(path).outputStream().use { out ->
        JsonModule.serialize(out, sourceUnit)
    }
}

private fun Repository.persistSnapshot() {
    print("Persisting latest repository snapshot...\n")
    snapshotDirectory.mkdirs()
    sourcesFile.printWriter().use { out ->
        val sources = listSources()
        for ((index, path) in sources.withIndex()) {
            out.println(path)
            persistSourceUnit(path)
            print("Persisted ${index + 1} sources...\r")
        }
        print("\n")
        print("Done!\n")
    }
}

private fun persistTransaction(transaction: Transaction) {
    val file = getTransactionFile(transaction.id)
    file.outputStream().use { out ->
        JsonModule.serialize(out, transaction)
    }
}

private fun Repository.persistHistory() {
    print("Persisting repository history...\n")
    historyFile.printWriter().use { out ->
        transactionsDirectory.mkdirs()
        for ((index, transaction) in getHistory().withIndex()) {
            out.println(transaction.id)
            persistTransaction(transaction)
            print("Persisted ${index + 1} transactions...\r")
        }
        print("\n")
        print("Done!\n")
    }
}
