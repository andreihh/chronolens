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
import org.chronolens.core.serialization.JsonException
import org.chronolens.core.serialization.JsonModule
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException

internal class RepositoryFileStorage(rootDirectory: File) : RepositoryStorage {
    private val storageDirectory = File(rootDirectory, STORAGE_ROOT_DIRECTORY)
    private val historyFile: File = File(storageDirectory, "HISTORY")
    private val revisionsDirectory: File = File(storageDirectory, "revisions")

    @Throws(IOException::class)
    private fun getRevisionFile(revisionId: String): File =
        File(revisionsDirectory, "$revisionId.json")

    private fun readRevision(revisionId: String): Revision =
        try {
            JsonModule.deserialize(getRevisionFile(revisionId))
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    @Throws(IOException::class)
    private fun writeRevision(revision: Revision) {
        getRevisionFile(revision.id.toString())
            .outputStream()
            .use { out -> JsonModule.serialize(out, revision) }
    }

    @Throws(IOException::class)
    override fun readHistoryIds(): List<String> =
        historyFile.readFileLines().dropLastWhile(String::isBlank)

    @Throws(IOException::class)
    override fun readHistory(): Sequence<Revision> =
        readHistoryIds().asSequence().map(::readRevision)

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

    companion object {
        /** The directory within the repository root where all storage files should be stored. */
        const val STORAGE_ROOT_DIRECTORY = ".chronolens"
    }
}

/**
 * Checks that the given [file] exists and is a file.
 *
 * @throws CorruptedRepositoryException if the given [file] doesn't exist or is not a file
 */
private fun checkFileExists(file: File) {
    checkState(file.isFile) { "File '$file' does not exist or is not a file!" }
}

/**
 * Creates the given [directory] and any parent directories, if necessary.
 *
 * @throws IOException if the given [directory] couldn't be created
 */
@Throws(IOException::class)
private fun mkdirs(directory: File) {
    if (!directory.exists() && !directory.mkdirs()) {
        throw IOException("Failed to create directory '$directory'!")
    }
}

/**
 * Delegates to [File.readLines] and keeps the lines up to the first empty line.
 *
 * @throws CorruptedRepositoryException if [this] file doesn't exist or is not a file
 * @throws IOException if any I/O errors occur
 */
@Throws(IOException::class)
private fun File.readFileLines(): List<String> {
    checkFileExists(this)
    return readLines().takeWhile(String::isNotEmpty)
}

/**
 * Delegates to [JsonModule.serialize].
 *
 * @throws CorruptedRepositoryException if the deserialization failed with a [JsonException] or the
 * given [src] file doesn't exist or is not a file
 * @throws IOException if any I/O errors occur
 */
@Throws(IOException::class)
private inline fun <reified T : Any> JsonModule.deserialize(src: File): T =
    try {
        checkFileExists(src)
        src.inputStream().use { deserialize(it) }
    } catch (e: JsonException) {
        throw CorruptedRepositoryException(e)
    }