/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.repository.RepositoryDatabaseFactory.DATABASE_ROOT_DIRECTORY
import org.chronolens.core.serialization.JsonModule
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException

internal class RepositoryFileDatabase(rootDirectory: File) : RepositoryDatabase {
    private val databaseDirectory = File(rootDirectory, DATABASE_ROOT_DIRECTORY)
    private val historyFile: File = File(databaseDirectory, "HISTORY")
    private val revisionsDirectory: File = File(databaseDirectory, "revisions")

    @Throws(IOException::class)
    private fun getRevisionsFile(revisionId: RevisionId): File =
        File(revisionsDirectory, "$revisionId.json")

    private fun readRevision(revisionId: RevisionId): Revision =
        try {
            getRevisionsFile(revisionId).inputStream().use { src -> JsonModule.deserialize(src) }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    @Throws(IOException::class)
    private fun writeRevision(revision: Revision) {
        getRevisionsFile(revision.id)
            .outputStream()
            .use { out -> JsonModule.serialize(out, revision) }
    }

    @Throws(IOException::class)
    override fun readHistory(): Sequence<Revision> =
        checkValidHistory(historyFile.readFileLines().dropLastWhile(String::isBlank))
            .asSequence()
            .map(::readRevision)

    @Throws(IOException::class)
    override fun writeHistory(revisions: Sequence<Revision>) {
        mkdirs(revisionsDirectory)
        historyFile.printWriter().use { out ->
            try {
                revisions.forEach { revision ->
                    out.println(revision.id)
                    writeRevision(revision)
                }
            } catch (e: UncheckedIOException) {
                throw IOException(e)
            }
        }
    }
}
