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

import org.chronolens.core.model.Project
import org.chronolens.core.model.ProjectEdit
import org.chronolens.core.model.sourcePath
import java.util.Collections.unmodifiableSet

/**
 * A transaction consisting of multiple changes applied to a [Project].
 *
 * @property revisionId the non-empty unique identifier of the revision
 * corresponding to this transaction, consisting of alphanumeric characters
 * @property date the date when the revision corresponding to this transaction
 * was committed in milliseconds since the Unix epoch
 * @property author the author of the revision corresponding to this transaction
 * @property edits the edits applied in this transaction
 * @throws IllegalArgumentException if [revisionId] is empty or contains
 * non-alphanumeric characters or if [date] is negative
 */
data class Transaction(
    val revisionId: String,
    val date: Long,
    val author: String,
    val edits: List<ProjectEdit> = emptyList()
) {

    init {
        validateRevisionId(revisionId)
        require(date >= 0L) { "'$revisionId' date '$date' can't be negative!" }
    }

    /** The set of source files modified in this transaction. */
    val changeSet: Set<String>
        get() = unmodifiableSet(edits.map(ProjectEdit::sourcePath).toSet())
}
