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

import org.chronolens.core.repository.Repository.Companion.isValidPath
import org.chronolens.core.repository.Repository.Companion.isValidRevisionId
import org.chronolens.core.versioning.Revision

/**
 * Validates the given revision [id].
 *
 * @throws IllegalArgumentException if the given [id] is invalid
 */
internal fun validateRevisionId(id: String) {
    require(isValidRevisionId(id)) { "Invalid revision id '$id'!" }
}

/**
 * Validates the given [path].
 *
 * @throws IllegalArgumentException if the given [path] is invalid
 */
internal fun validatePath(path: String): String {
    require(isValidPath(path)) { "Invalid source file path '$path'!" }
    return path
}

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
internal fun checkValidRevisionId(id: String): String {
    checkState(isValidRevisionId(id)) { "Invalid revision id '$id'!" }
    return id
}

/**
 * Checks that the given source file [path] is valid.
 *
 * @throws CorruptedRepositoryException if the given [path] is invalid
 */
internal fun checkValidPath(path: String): String {
    checkState(isValidPath(path)) { "Invalid source file path '$path'!" }
    return path
}

/**
 * Checks that the given [sources] are valid.
 *
 * @throws CorruptedRepositoryException if the given [sources] contain any
 * invalid or duplicated paths
 */
internal fun checkValidSources(sources: Collection<String>): Set<String> {
    val sourceFiles = LinkedHashSet<String>(sources.size)
    for (source in sources) {
        checkValidPath(source)
        checkState(source !in sourceFiles) {
            "Duplicated source file '$source'!"
        }
        sourceFiles += source
    }
    return sourceFiles
}

/**
 * Checks that the given list of transaction ids represent a valid [history].
 *
 * @throws CorruptedRepositoryException if the given [history] is invalid
 */
internal fun checkValidHistory(history: List<String>): List<String> {
    val revisionIds = HashSet<String>(history.size)
    for (id in history) {
        checkState(id !in revisionIds) { "Duplicated revision id '$id'!" }
        checkValidRevisionId(id)
        revisionIds += id
    }
    return history
}

/**
 * Checks that the given list of revisions represent a valid [history].
 *
 * @throws CorruptedRepositoryException if the given [history] contains invalid
 * or duplicated revision ids
 */
@JvmName("checkValidRevisionHistory")
internal fun checkValidHistory(history: List<Revision>): List<Revision> {
    checkValidHistory(history.map(Revision::id))
    return history
}
