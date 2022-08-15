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
import org.chronolens.core.model.Revision
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
        persistHistory()
    }

    private fun persistRevision(revision: Revision) {
        val file = schema.getRevisionsFile(revision.id)
        file.outputStream().use { out -> JsonModule.serialize(out, revision) }
    }

    private fun persistHistory() {
        listener?.onHistoryStart(repository.listRevisions().size)
        mkdirs(schema.revisionsDirectory)
        schema.historyFile.printWriter().use { out ->
            for (revision in repository.getHistory()) {
                out.println(revision.id)
                persistRevision(revision)
                listener?.onRevisionPersisted(revision.id)
            }
        }
        listener?.onHistoryEnd()
    }
}
