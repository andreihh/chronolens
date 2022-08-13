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

package org.chronolens.core.model

import java.time.Instant

/**
 * A revision consisting of multiple changes applied to a [SourceTree].
 *
 * @property id the unique identifier of this revision within a repository
 * @property date the date when this revision was committed
 * @property author the author of this revision
 * @property edits the ordered edits applied in this revision
 */
public data class Revision(
    val id: RevisionId,
    val date: Instant,
    val author: String,
    val edits: List<SourceTreeEdit> = emptyList()
) {

    /** The set of source files modified in this revision. */
    public val changeSet: Set<SourcePath>
        get() = edits.map(SourceTreeEdit::sourcePath).toSet()
}

/**
 * The unique identifier of a committed revision in a repository (usually tracked by a version
 * control system).
 *
 * Compatible with Git, Mercurial, SVN, CVS, and Perforce.
 *
 * @throws IllegalArgumentException if the given [id] is invalid
 */
public data class RevisionId(private val id: String) {
    init {
        require(isValid(id)) { "Invalid revision id '$id'!" }
    }

    override fun toString(): String = id

    public companion object {
        /**
         * Returns whether the given [revisionId] is valid (non-empty, consisting of alphanumeric
         * characters and dashes).
         */
        @JvmStatic
        public fun isValid(revisionId: String): Boolean = revisionId.matches(revisionIdRegex)
    }
}

private val revisionIdRegex = Regex("[-\\w]+")
