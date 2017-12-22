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

import org.metanalysis.core.model.Project
import org.metanalysis.core.model.ProjectEdit

/**
 * A transaction consisting of multiple changes applied to a [Project].
 *
 * @property id the non-empty unique identifier of this transaction consisting
 * of alphanumeric characters
 * @property date the date at which this transaction was committed in
 * milliseconds since the Unix epoch
 * @property author the author of the transaction
 * @property edits the edits applied in this transaction
 * @throws IllegalArgumentException if `id` is empty or contains
 * non-alphanumeric characters or if `date` is negative
 */
data class Transaction(
        val id: String,
        val date: Long,
        val author: String,
        val edits: List<ProjectEdit> = emptyList()
) {
    init {
        validateTransactionId(id)
        require(date >= 0L) { "'$id' date '$date' can't be negative!" }
    }
}
