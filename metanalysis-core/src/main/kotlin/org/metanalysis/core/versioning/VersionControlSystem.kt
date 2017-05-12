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
import java.util.ServiceConfigurationError
import java.util.ServiceLoader

/**
 * An abstract version control system (VCS) which interacts with the repository
 * detected in the current working directory.
 *
 * A VCS operates with the concept of `revision`, which are objects that can be
 * dereferenced to commits (a commit, branch, tag etc.).
 *
 * Version control systems must have a public no-arg constructor.
 *
 * The file
 * `META-INF/services/org.metanalysis.core.versioning.VersionControlSystem` must
 * be provided and must contain the list of all provided VCS implementations.
 */
abstract class VersionControlSystem {
    companion object {
        private val vcsList by lazy {
            ServiceLoader.load(VersionControlSystem::class.java)
                    .filter(VersionControlSystem::isSupported)
        }

        /**
         * Returns the VCS for the repository detected in the current working
         * directory.
         *
         * This method is the only safe way of getting VCS instances. Calling
         * methods on a VCS instance which is not supported or detected will
         * lead to undefined results.
         *
         * @return the requested VCS, or `null` if no supported VCS repository
         * was detected or if multiple repositories were detected
         * @throws ServiceConfigurationError if the configuration file couldn't
         * be loaded properly
         * @throws IllegalThreadStateException if the VCS process is interrupted
         * @throws IOException if any input related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic fun detect(): VersionControlSystem? =
                vcsList.filter(VersionControlSystem::detectRepository)
                        .singleOrNull()
    }

    /**
     * Returns whether this VCS is supported in this environment.
     *
     * @throws IllegalThreadStateException if the VCS process is interrupted
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    protected abstract fun isSupported(): Boolean

    /**
     * Returns whether a repository was detected in the current working
     * directory.
     *
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    protected abstract fun detectRepository(): Boolean

    /**
     * Returns the `head` revision.
     *
     * @return the `head` revision
     * @throws IllegalStateException if the `head` revision doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getHead(): Revision

    /**
     * Returns the revision with the given `revisionId`.
     *
     * @param revisionId the id of the requested revision
     * @return the requested revision
     * @throws RevisionNotFoundException if the requested revision doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getRevision(revisionId: String): Revision

    /**
     * Returns all the existing files in the `head` revision.
     *
     * @return the set of existing files in `revision`
     * @throws IllegalArgumentException if `revision` wasn't created by this VCS
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun listFiles(): Set<String>

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
    abstract fun getFile(revisionId: String, path: String): String?

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
    abstract fun getFileHistory(path: String): List<Revision>
}
