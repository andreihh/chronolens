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
import org.chronolens.core.repository.PersistentRepository.ProgressListener
import org.chronolens.core.serialization.JsonException
import org.chronolens.core.serialization.JsonModule
import java.io.File
import java.io.FileNotFoundException
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
@Throws(IOException::class) internal constructor(
    private val files: RepositoryFileLayout,
) : Repository {

    private val headId: String
    private val sources: Set<String>
    private val history: List<String>

    init {
        with(files) {
            checkDirectoryExists(rootDirectory)
            checkFileExists(headFile)
            checkFileExists(sourcesFile)
            checkFileExists(historyFile)
            checkDirectoryExists(snapshotDirectory)
            checkDirectoryExists(transactionsDirectory)
        }

        val rawHeadId = files.headFile.readFileLines()
        checkState(rawHeadId.size == 1) {
            "'${files.headFile}' must contain a single line with the head id!"
        }
        headId = checkValidRevisionId(rawHeadId.single())

        val rawSources = files.sourcesFile.readFileLines()
        sources = unmodifiableSet(checkValidSources(rawSources))
        sources.forEach { path -> checkFileExists(files.getSourceFile(path)) }

        val rawHistory = files.historyFile.readFileLines()
        history = unmodifiableList(checkValidHistory(rawHistory))
        history.forEach { revisionId ->
            checkFileExists(files.getTransactionFile(revisionId))
        }
    }

    override fun getHeadId(): String = headId

    override fun listSources(): Set<String> = sources

    override fun listRevisions(): List<String> = history

    override fun getSource(path: String): SourceFile? {
        validatePath(path)
        return if (path !in sources) null
        else JsonModule.deserialize(files.getSourceFile(path))
    }

    override fun getHistory(): Sequence<Transaction> =
        history.asSequence().map { transactionId ->
            val file = files.getTransactionFile(transactionId)
            try {
                JsonModule.deserialize<Transaction>(file)
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }

    public companion object {
        /**
         * Returns the persisted repository detected in the given
         * [repositoryDirectory], or `null` if no repository was detected.
         *
         * @throws IOException if any input related errors occur
         * @throws CorruptedRepositoryException if the repository is corrupted
         */
        @Throws(IOException::class)
        @JvmStatic
        public fun load(repositoryDirectory: File): PersistentRepository? {
            val files = RepositoryFileLayout(repositoryDirectory)
            return if (!files.rootDirectory.isDirectory) null
            else PersistentRepository(files)
        }

        /**
         * Persists this repository in the given [repositoryDirectory].
         *
         * @receiver the repository to persist
         * @param repositoryDirectory the directory where the repository should
         * be persisted
         * @param listener the listener which will be notified on the
         * persistence progress, or `null` if no notification is required
         * @return the persisted repository
         * @throws IOException if any output related errors occur
         * @throws CorruptedRepositoryException if the repository is corrupted
         */
        @Throws(IOException::class)
        @JvmStatic
        public fun Repository.persist(
            repositoryDirectory: File,
            listener: ProgressListener? = null
        ): PersistentRepository {
            val files = RepositoryFileLayout(repositoryDirectory)
            if (this is PersistentRepository && files == this.files) {
                return this
            }
            RepositoryPersister(this, files, listener).persist()
            return PersistentRepository(files)
        }

        /**
         * Deletes the previously persisted repository from the given
         * [repositoryDirectory].
         *
         * All corresponding [PersistentRepository] instances will become
         * corrupted after this method is called.
         *
         * @throws IOException if any input or output related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic
        public fun clean(repositoryDirectory: File) {
            val files = RepositoryFileLayout(repositoryDirectory)
            files.rootDirectory.deleteRecursively()
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

private fun checkFileExists(file: File) {
    checkState(file.isFile) {
        "File '$file' does not exist or is not a file!"
    }
}

private fun checkDirectoryExists(directory: File) {
    checkState(directory.isDirectory) {
        "Directory '$directory' does not exist or is not a directory!"
    }
}

private inline fun <reified T : Any> JsonModule.deserialize(src: File): T =
    try {
        src.inputStream().use { deserialize(it) }
    } catch (e: JsonException) {
        throw CorruptedRepositoryException(e)
    } catch (e: FileNotFoundException) {
        throw CorruptedRepositoryException(e)
    }

private fun File.readFileLines(): List<String> =
    readLines().takeWhile(String::isNotEmpty)
