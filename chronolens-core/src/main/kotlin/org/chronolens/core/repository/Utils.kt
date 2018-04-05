/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
 * @throws IllegalArgumentException if the given [id] is not valid
 */
internal fun validateRevisionId(id: String) {
    require(isValidRevisionId(id)) { "Invalid revision id '$id'!" }
}

/**
 * Checks that the given revision [id] is valid.
 *
 * @throws IllegalStateException if the given [id] is not valid
 */
internal fun checkValidRevisionId(id: String) {
    check(isValidRevisionId(id)) { "Invalid revision id '$id'!" }
}

/**
 * Validates the given [path].
 *
 * @throws IllegalArgumentException if the given [path] is not valid
 */
internal fun validatePath(path: String) {
    require(isValidPath(path)) { "Invalid source file path '$path'!" }
}

/**
 * Checks that the given source file [path] is valid.
 *
 * @throws IllegalStateException if the given [path] is not valid
 */
internal fun checkValidPath(path: String) {
    check(isValidPath(path)) { "Invalid source file path '$path'!" }
}

/**
 * Checks that the given list of transaction ids represent a valid [history].
 *
 * @throws IllegalStateException if the given [history] is invalid
 */
internal fun checkValidHistory(history: List<String>) {
    val revisionIds = HashSet<String>(history.size)
    for (id in history) {
        check(id !in revisionIds) { "Duplicated revision id '$id'!" }
        checkValidRevisionId(id)
        revisionIds += id
    }
}

/**
 * Checks that the given list of revisions represent a valid [history].
 *
 * @throws IllegalStateException if the given [history] is invalid
 */
@JvmName("checkValidRevisionHistory")
internal fun checkValidHistory(history: List<Revision>) {
    checkValidHistory(history.map(Revision::id))
}

/**
 * Returns a lazy view of this [Iterable] with each element mapped using the
 * given [transform].
 */
internal fun <T, R> Iterable<T>.mapLazy(transform: (T) -> R): Iterable<R> =
    Iterable {
        val it = iterator()
        object : Iterator<R> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): R = transform(it.next())
        }
    }
