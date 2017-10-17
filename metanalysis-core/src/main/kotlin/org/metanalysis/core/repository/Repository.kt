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
import org.metanalysis.core.model.SourceNode.SourceUnit
import org.metanalysis.core.model.Transaction

/**
 * A wrapper which connects to a repository and allows querying source code
 * metadata and the project history.
 */
interface Repository {
    /** Returns the id of the `head` revision of this repository. */
    fun getHeadId(): String

    /**
     * Returns the set of source units from the `head` revision which can be
     * interpreted.
     */
    fun listSources(): Set<String>

    /**
     * Returns the source unit found at the given `path`, as it is found in the
     * `head` revision.
     *
     * If the source contains syntax errors, then the most recent version which
     * can be parsed without errors will be returned. If all versions of the
     * source contain errors, then the empty source unit will be returned.
     *
     * @param path the path of the requested source unit
     * @return the requested source unit, or `null` if the `path` doesn't exist
     * in the `head` revision or couldn't be interpreted
     */
    fun getSourceUnit(path: String): SourceUnit?

    /**
     * Returns the snapshot of the repository, as it is found in the `head`
     * revision.
     *
     * @see getSourceUnit for details about how the latest source units are
     * retrieved
     */
    fun getSnapshot(): Project {
        val units = listSources().map(this::getSourceUnit).requireNoNulls()
        return Project(units)
    }

    /**
     * Returns a lazy view of the transactions applied to the repository in
     * chronological order.
     */
    fun getHistory(): Iterable<Transaction>
}
