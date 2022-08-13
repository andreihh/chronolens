/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import java.io.File
import java.io.IOException
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.TransactionId
import org.chronolens.core.serialization.JsonException
import org.chronolens.core.serialization.JsonModule
import org.chronolens.core.versioning.Revision

/**
 * Checks that the given [condition] is true.
 *
 * @throws CorruptedRepositoryException if [condition] is false
 */
internal fun checkState(condition: Boolean, lazyMessage: () -> String) {
    if (!condition) throw CorruptedRepositoryException(lazyMessage())
}

/**
 * Checks that the given revision [id] is valid.
 *
 * @throws CorruptedRepositoryException if the given [id] is invalid
 */
internal fun checkValidRevisionId(id: String): TransactionId {
    checkState(TransactionId.isValid(id)) { "Invalid revision id '$id'!" }
    return TransactionId(id)
}

/**
 * Checks that the given source file [path] is valid.
 *
 * @throws CorruptedRepositoryException if the given [path] is invalid
 */
internal fun checkValidPath(path: String): SourcePath {
    checkState(SourcePath.isValid(path)) { "Invalid source file path '$path'!" }
    return SourcePath(path)
}

/**
 * Checks that the given [sources] are valid.
 *
 * @throws CorruptedRepositoryException if the given [sources] contain any invalid or duplicated
 * paths
 */
internal fun checkValidSources(sources: Collection<String>): Set<SourcePath> {
    val sourceFiles = LinkedHashSet<SourcePath>(sources.size)
    for (source in sources.map(::checkValidPath)) {
        checkState(source !in sourceFiles) { "Duplicated source file '$source'!" }
        sourceFiles += source
    }
    return sourceFiles
}

/**
 * Checks that the given list of transaction ids represent a valid [history].
 *
 * @throws CorruptedRepositoryException if the given [history] is invalid
 */
internal fun checkValidHistory(history: List<String>): List<TransactionId> {
    val revisionIds = HashSet<String>(history.size)
    for (id in history) {
        checkState(id !in revisionIds) { "Duplicated revision id '$id'!" }
        checkValidRevisionId(id)
        revisionIds += id
    }
    return history.map(::TransactionId)
}

/**
 * Checks that the given list of revisions represent a valid [history].
 *
 * @throws CorruptedRepositoryException if the given [history] contains invalid or duplicated
 * revision ids
 */
@JvmName("checkValidRevisionHistory")
internal fun checkValidHistory(history: List<Revision>): List<Revision> {
    checkValidHistory(history.map(Revision::id))
    return history
}

/**
 * Checks that [this] collection doesn't contain any `null` elements.
 *
 * @throws CorruptedRepositoryException if any element is `null`
 */
internal fun <T : Any> Collection<T?>.checkNoNulls(): Collection<T> {
    for (item in this) {
        if (item == null) {
            throw CorruptedRepositoryException("'null' found in '$this'!")
        }
    }
    @Suppress("UNCHECKED_CAST") return this as Collection<T>
}

/**
 * Checks that the given [file] exists and is a file.
 *
 * @throws CorruptedRepositoryException if the given [file] doesn't exist or is not a file
 */
internal fun checkFileExists(file: File) {
    checkState(file.isFile) { "File '$file' does not exist or is not a file!" }
}

/**
 * Delegates to [JsonModule.serialize].
 *
 * @throws CorruptedRepositoryException if the deserialization failed with a [JsonException] or the
 * given [src] file doesn't exist or is not a file
 * @throws IOException if any I/O errors occur
 */
@Throws(IOException::class)
internal inline fun <reified T : Any> JsonModule.deserialize(src: File): T =
    try {
        checkFileExists(src)
        src.inputStream().use { deserialize(it) }
    } catch (e: JsonException) {
        throw CorruptedRepositoryException(e)
    }

/**
 * Delegates to [File.readLines] and keeps the lines up to the first empty line.
 *
 * @throws CorruptedRepositoryException if [this] file doesn't exist or is not a file
 * @throws IOException if any I/O errors occur
 */
@Throws(IOException::class)
internal fun File.readFileLines(): List<String> {
    checkFileExists(this)
    return readLines().takeWhile(String::isNotEmpty)
}

/**
 * Creates the given [directory] and any parent directories, if necessary.
 *
 * @throws IOException if the given [directory] couldn't be created
 */
@Throws(IOException::class)
internal fun mkdirs(directory: File) {
    if (!directory.exists() && !directory.mkdirs()) {
        throw IOException("Failed to create directory '$directory'!")
    }
}
