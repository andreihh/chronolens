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

import java.io.FileNotFoundException
import java.io.IOException
import java.util.Date
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
        @JvmStatic fun get(): VersionControlSystem? =
                vcsList.filter(VersionControlSystem::detectRepository)
                        .singleOrNull()
    }

    /** A revision in a version control system (commit, tag, branch etc.). */
    inner class Revision internal constructor(revisionId: String) {
        /** The unique id of this revision. */
        val id: String

        /** The date at which this revision was created. */
        val date: Date

        /** The author of this revision. */
        val author: String

        init {
            val line = getRawRevision(revisionId)
            val (rawId, rawDate, rawAuthor) = line.split(':', limit = 3)
            id = rawId
            date = Date(rawDate.toLong() * 1000)
            author = rawAuthor
        }

        internal fun isCreatedBy(vcs: VersionControlSystem): Boolean =
                this@VersionControlSystem == vcs

        override fun equals(other: Any?): Boolean =
                other is Revision && id == other.id
                        && other.isCreatedBy(this@VersionControlSystem)

        override fun hashCode(): Int = id.hashCode()

        override fun toString(): String =
                "Revision(id=$id, date=$date, author=$author)"
    }

    /**
     * Validates the given `revision`.
     *
     * @param revision the revision which should be validated
     * @throws IllegalArgumentException if `revision` wasn't created by this VCS
     */
    protected fun validateRevision(revision: Revision) {
        require(revision.isCreatedBy(this)) { "Invalid revision $revision!" }
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
     * @throws IllegalThreadStateException if the VCS process is interrupted
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    protected abstract fun detectRepository(): Boolean

    /**
     * Returns the raw representation of a revision in the following format:
     * `<id>:<seconds since epoch>:<author name>`.
     *
     * @param revisionId the id of the requested revision
     * @return the raw representation of the requested revision
     * @throws IllegalThreadStateException if the VCS process is interrupted
     * @throws RevisionNotFoundException if the requested revision doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    protected abstract fun getRawRevision(revisionId: String): String

    /**
     * Returns the `head` revision.
     *
     * @return the `head` revision
     * @throws IllegalStateException if the `head` revision doesn't exist
     * @throws IllegalThreadStateException if the VCS process is interrupted
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getHead(): Revision

    /**
     * Returns the revision with the given `revisionId`.
     *
     * @param revisionId the id of the requested revision
     * @return the requested revision
     * @throws IllegalThreadStateException if the VCS process is interrupted
     * @throws RevisionNotFoundException if the requested revision doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun getRevision(revisionId: String): Revision = Revision(revisionId)

    /**
     * Returns the content of the file located at the given `path` as it is
     * found in `revision`.
     *
     * @param revision the desired revision of the file
     * @param path the relative path of the requested file
     * @return the content of the requested file, or `null` if it doesn't exist
     * in `revision`
     * @throws IllegalArgumentException if `revision` wasn't created by this VCS
     * @throws IllegalThreadStateException if the VCS process is interrupted
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getFile(revision: Revision, path: String): String?

    /**
     * Returns all the revisions which modified the file at the given `path` up
     * to the given `revision`.
     *
     * The revisions are given chronologically.
     *
     * @throws IllegalArgumentException if `revision` wasn't created by this VCS
     * @throws IllegalThreadStateException if the VCS process is interrupted
     * @throws FileNotFoundException if `path` never existed in `revision` or
     * any of its ancestors
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getFileHistory(
            revision: Revision,
            path: String
    ): List<Revision>

    /**
     * Returns all the existing files in `revision`.
     *
     * @param revision the inspected revision
     * @return the set of existing files in `revision`
     * @throws IllegalArgumentException if `revision` wasn't created by this VCS
     * @throws IllegalThreadStateException if the VCS process is interrupted
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun listFiles(revision: Revision): Set<String>
}
