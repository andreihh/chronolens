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

import java.time.Instant
import java.util.Collections.unmodifiableSet
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.SourceTreeEdit

/**
 * A transaction consisting of multiple changes applied to a [SourceTree].
 *
 * @property revisionId the non-empty unique identifier of the revision corresponding to this
 * transaction, consisting of alphanumeric characters
 * @property date the date when the revision corresponding to this transaction was committed
 * @property author the author of the revision corresponding to this transaction
 * @property edits the edits applied in this transaction
 * @throws IllegalArgumentException if [revisionId] is empty or contains non-alphanumeric characters
 */
public data class Transaction(
    val revisionId: String,
    val date: Instant,
    val author: String,
    val edits: List<SourceTreeEdit> = emptyList(),
) {

    init {
        validateRevisionId(revisionId)
    }

    /** The set of source files modified in this transaction. */
    public val changeSet: Set<SourcePath>
        get() = unmodifiableSet(edits.map(SourceTreeEdit::sourcePath).toSet())
}
