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

package org.metanalysis.core.versioning

import java.io.IOException

/**
 * A version control system (VCS) proxy which interacts with the repository
 * detected in the current working directory.
 *
 * The associated VCS must be supported in the current environment and the
 * detected repository must be in a valid state.
 */
interface VcsProxy {
    /**
     * Returns the `head` revision.
     *
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun getHead(): Revision

    /**
     * Returns the revision with the given id.
     *
     * @param revisionId the id of the requested revision
     * @throws RevisionNotFoundException if the requested revision doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun getRevision(revisionId: String): Revision

    /**
     * Returns the set of existing files in the [head][getHead] revision.
     *
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun listFiles(): Set<String>

    /**
     * Returns the file located at the given path as it is found in the given
     * revision.
     *
     * @param revisionId the id of the desired revision of the file
     * @param path the path of the requested file, relative to the repository
     * root
     * @return the content of the requested file, or `null` if it doesn't exist
     * in the specified revision
     * @throws RevisionNotFoundException if `revisionId` doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun getFile(revisionId: String, path: String): String?

    /**
     * Returns all the revisions which modified the file at the given path up to
     * the [head][getHead] revision.
     *
     * @param path the path of the requested file, relative to the repository
     * root
     * @return the chronological list of revisions which modified the requested
     * file, or the empty list if `path` never existed in the [head][getHead]
     * revision or any of its ancestors
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun getFileHistory(path: String): List<Revision>
}
