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

import org.metanalysis.core.model.isValidPath
import org.metanalysis.core.versioning.Revision.Companion.isValidRevisionId

/** Returns whether the given transaction `id` is valid. */
internal fun isValidTransactionId(id: String): Boolean = isValidRevisionId(id)

/**
 * Checks that the given transaction `id` is valid.
 *
 * @throws IllegalStateException if the given `id` is not valid
 */
internal fun validateTransactionId(id: String) {
    check(isValidTransactionId(id)) { "Invalid transaction id '$id'!" }
}

/**
 * Checks that the given source file `path` is valid.
 *
 * @throws IllegalStateException if the given `path` is not valid
 */
internal fun validatePath(path: String) {
    check(isValidPath(path)) { "Invalid source file path '$path'!" }
}

internal fun validateHistory(history: List<String>) {
    val transactionIds = hashSetOf<String>()
    for (id in history) {
        check(id !in transactionIds) { "Duplicated transaction id '$id'!" }
        validateTransactionId(id)
        transactionIds += id
    }
}

/**
 * Returns a lazy view of this `Iterable` with each element mapped using the
 * given `transform`.
 */
internal fun <T, R> Iterable<T>.mapLazy(transform: (T) -> R): Iterable<R> =
        Iterable {
            val it = iterator()
            object : Iterator<R> {
                override fun hasNext(): Boolean = it.hasNext()
                override fun next(): R = transform(it.next())
            }
        }
