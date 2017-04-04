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
import java.util.ServiceLoader

/**
 * An abstract version control system which interacts with the repository
 * detected in the current working directory.
 *
 * A version control system operates with the concept of `revision`, which are
 * objects that can be dereferenced to commits (a commit, branch, tag etc.).
 *
 * Version control systems must have a public no-arg constructor.
 *
 * The file
 * `META-INF/services/org.metanalysis.core.versioning.VersionControlSystem` must
 * be provided and must contain the list of all provided version control
 * system implementations.
 */
abstract class VersionControlSystem {
    companion object {
        private val vcss = ServiceLoader.load(VersionControlSystem::class.java)
        private val nameToVcs = vcss.associateBy(VersionControlSystem::name)
                .filterValues(VersionControlSystem::isSupported)

        /**
         * Returns the version control system with the given `name`.
         *
         * @param name the name of the requested version control system
         * @return the requested version control system, or `null` if no such
         * system was provided or if it isn't supported in this environment
         */
        @JvmStatic fun getByName(name: String): VersionControlSystem? =
                nameToVcs[name]

        /**
         * Returns the version control system for the repository in the current
         * working directory.
         *
         * @return the requested version control system, or `null` if no such
         * system could be identified or if multiple systems have been
         * identified
         */
        @JvmStatic fun get(): VersionControlSystem? = nameToVcs.values
                .filter(VersionControlSystem::detectRepository).singleOrNull()
    }

    /**
     * @param T
     */
    protected abstract class Subprocess<out T : Any> {
        companion object {
            @Throws(IOException::class)
            fun execute(vararg command: String): Boolean {
                val process = ProcessBuilder().command(*command).start()
                try {
                    return process.waitFor() == 0
                } catch (e: InterruptedException) {
                    process.destroy()
                    throw IOException(e)
                } finally {
                    process.inputStream.close()
                    process.errorStream.close()
                    process.outputStream.close()
                }
            }
        }

        protected abstract val command: List<String>

        @Throws(IOException::class)
        protected abstract fun onSuccess(input: String): T

        @Throws(IOException::class)
        protected open fun onError(error: String): Nothing =
                throw IOException(error)

        @Throws(IOException::class)
        fun run(): T = with(ProcessBuilder().command(command).start()) {
            try {
                val text = inputStream.bufferedReader().readText()
                if (waitFor() == 0) onSuccess(text)
                else onError(errorStream.bufferedReader().readText())
            } catch (e: InterruptedException) {
                destroy()
                throw IOException(e)
            } finally {
                inputStream.close()
                errorStream.close()
                outputStream.close()
            }
        }
    }

    /** The name of this version control system. */
    abstract val name: String

    /**
     * Returns whether this version control system is supported in this
     * environment.
     *
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
    abstract fun detectRepository(): Boolean

    /**
     * Returns the currently checked out commit.
     *
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getHead(): Commit

    /**
     * Returns the commit which corresponds to the given `revision`.
     *
     * @param revision the inspected revision
     * @return the corresponding commit
     * @throws RevisionNotFoundException if the given `revision` doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getCommit(revision: String): Commit

    /**
     * Returns the all the existing files in the given `revision`.
     *
     * @param revision the inspected revision
     * @return the set of files existing in the `revision`
     * @throws RevisionNotFoundException if the given `revision` doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun listFiles(revision: String): Set<String>

    /**
     * Returns the content of the file located at the given `path` as it is
     * found in the given `revision`.
     *
     * @param revision the desired revision of the file
     * @param path the relative path of the requested file
     * @throws RevisionNotFoundException if the given `revision` doesn't exist
     * @throws FileNotFoundException if the given `path` doesn't exist in the
     * given `revision`
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getFile(revision: String, path: String): String

    /**
     * Returns all the commits which modified the file at the given `path`, up
     * to the given `revision`.
     *
     * The commits are given chronologically.
     *
     * @throws RevisionNotFoundException if the given `revision` doesn't exist
     * @throws FileNotFoundException if the given `path` never existed in the
     * given `revision` or any of its ancestor revisions
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getFileHistory(revision: String, path: String): List<Commit>
}
