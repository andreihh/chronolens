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
import java.util.ServiceLoader

/**
 * An abstract version control system (VCS) which interacts with the repository
 * detected in the current working directory.
 *
 * A VCS operates with the concept of `revision`, which are objects that can be
 * dereferenced to commits (a commit, branch, tag etc.).
 */
interface VcsProxy {
    /**
     * Returns the `head` revision.
     *
     * @return the `head` revision
     * @throws RevisionNotFoundException if the `head` revision doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun getHead(): Revision

    /**
     * Returns the revision with the given `revisionId`.
     *
     * @param revisionId the id of the requested revision
     * @return the requested revision
     * @throws RevisionNotFoundException if the requested revision doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun getRevision(revisionId: String): Revision

    /**
     * Returns all the existing files in the `head` revision.
     *
     * @return the set of existing files in the `head` revision
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun listFiles(): Set<String>

    /**
     * Returns the content of the file located at the given `path` as it is
     * found in `revision`.
     *
     * @param revisionId the id of the desired revision of the file
     * @param path the relative path of the requested file
     * @return the content of the requested file, or `null` if it doesn't exist
     * in `revision`
     * @throws RevisionNotFoundException if `revisionId` doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun getFile(revisionId: String, path: String): String?

    /**
     * Returns all the revisions which modified the file at the given `path` up
     * to the `head` revision.
     *
     * The revisions are given chronologically.
     *
     * @return the list of revisions which modified the file at the given
     * `path`, or the empty list if `path` never existed in the `head` revision
     * or any of its ancestors
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun getFileHistory(path: String): List<Revision>
}
