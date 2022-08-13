/*
 * Copyright 2021-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.Transaction
import org.chronolens.core.repository.PersistentRepository.ProgressListener
import org.chronolens.core.serialization.JsonModule

internal class RepositoryPersister(
    private val repository: Repository,
    private val schema: RepositoryFileSchema,
    private val listener: ProgressListener?,
) {

    @Throws(IOException::class)
    fun persist() {
        mkdirs(schema.rootDirectory)
        schema.headFile.printWriter().use { out -> out.println(repository.getHeadId()) }
        persistSnapshot()
        persistHistory()
    }

    private fun persistSource(source: SourceFile) {
        mkdirs(schema.getSourceDirectory(source.path))
        schema.getSourceFile(source.path).outputStream().use { out ->
            JsonModule.serialize(out, source)
        }
    }

    private fun persistSnapshot() {
        listener?.onSnapshotStart(
            headId = repository.getHeadId(),
            sourceCount = repository.listSources().size,
        )
        mkdirs(schema.snapshotDirectory)
        schema.sourcesFile.printWriter().use { out ->
            for (path in repository.listSources()) {
                val source =
                    repository.getSource(path)
                        ?: throw CorruptedRepositoryException("'$path' couldn't be interpreted!")
                out.println(path)
                persistSource(source)
                listener?.onSourcePersisted(path)
            }
        }
        listener?.onSnapshotEnd()
    }

    private fun persistTransaction(transaction: Transaction) {
        val file = schema.getTransactionFile(transaction.id)
        file.outputStream().use { out -> JsonModule.serialize(out, transaction) }
    }

    private fun persistHistory() {
        listener?.onHistoryStart(repository.listRevisions().size)
        mkdirs(schema.transactionsDirectory)
        schema.historyFile.printWriter().use { out ->
            for (transaction in repository.getHistory()) {
                out.println(transaction.id)
                persistTransaction(transaction)
                listener?.onTransactionPersisted(transaction.id)
            }
        }
        listener?.onHistoryEnd()
    }
}
