/*
 * Copyright 2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import org.chronolens.core.serialization.JsonModule
import java.io.File
import java.io.IOException

internal class RepositoryPersister(
    private val repository: Repository,
    private val files: RepositoryFileLayout,
    private val listener: ProgressListener?,
) {

    @Throws(IOException::class)
    fun persist() {
        mkdirs(files.rootDirectory)
        files.headFile.printWriter().use { out ->
            out.println(repository.getHeadId())
        }
        persistSnapshot()
        persistHistory()
    }

    private fun persistSource(source: SourceFile) {
        mkdirs(files.getSourceDirectory(source.path))
        files.getSourceFile(source.path).outputStream().use { out ->
            JsonModule.serialize(out, source)
        }
    }

    private fun persistSnapshot() {
        listener?.onSnapshotStart(
            headId = repository.getHeadId(),
            sourceCount = repository.listSources().size,
        )
        mkdirs(files.snapshotDirectory)
        files.sourcesFile.printWriter().use { out ->
            for (path in repository.listSources()) {
                val source = repository.getSource(path)
                    ?: error("'$path' couldn't be interpreted!")
                out.println(path)
                persistSource(source)
                listener?.onSourcePersisted(path)
            }
        }
        listener?.onSnapshotEnd()
    }

    private fun persistTransaction(transaction: Transaction) {
        val file = files.getTransactionFile(transaction.revisionId)
        file.outputStream().use { out ->
            JsonModule.serialize(out, transaction)
        }
    }

    private fun persistHistory() {
        listener?.onHistoryStart(repository.listRevisions().size)
        mkdirs(files.transactionsDirectory)
        files.historyFile.printWriter().use { out ->
            for (transaction in repository.getHistory()) {
                out.println(transaction.revisionId)
                persistTransaction(transaction)
                listener?.onTransactionPersisted(transaction.revisionId)
            }
        }
        listener?.onHistoryEnd()
    }
}

private fun mkdirs(directory: File) {
    if (!directory.exists() && !directory.mkdirs()) {
        throw IOException("Failed to create directory '$directory'!")
    }
}
