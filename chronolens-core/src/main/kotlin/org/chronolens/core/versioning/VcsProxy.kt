/*
 * Copyright 2017-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.versioning

/**
 * A version control system (VCS) proxy which interacts with the repository detected in a specified
 * directory (repository root).
 *
 * The associated VCS must be supported in the current environment and the detected repository must
 * be in a valid state.
 */
public interface VcsProxy {
    /** Returns the `head` revision. */
    public fun getHead(): VcsRevision

    /** Returns the revision with the given [revisionId], or `null` if no such revision exists. */
    public fun getRevision(revisionId: String): VcsRevision?

    /**
     * Returns the files inside the current working directory which were modified in the revision
     * with the given [revisionId].
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     */
    public fun getChangeSet(revisionId: String): Set<String>

    /**
     * Returns the set of existing files in the current working directory in the revision with the
     * given [revisionId].
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     */
    public fun listFiles(revisionId: String): Set<String>

    /**
     * Returns the content of the file located at the given relative [path] as it is found in the
     * revision with the given [revisionId], or `null` if it doesn't exist in the specified
     * revision.
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     */
    public fun getFile(revisionId: String, path: String): String?

    /**
     * Returns the chronological list of revisions which modified the file or directory at the given
     * [path] up to and including the given [revisionId], or the empty list if [path] never existed
     * in the given revision or any of its ancestors.
     *
     * A directory is modified if any file in its subtree is modified. The empty string is a path
     * that represents the repository root.
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     */
    public fun getHistory(revisionId: String = getHead().id, path: String = ""): List<VcsRevision>
}
