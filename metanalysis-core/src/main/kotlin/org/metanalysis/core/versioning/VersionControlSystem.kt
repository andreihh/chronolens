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
import java.io.InputStream
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
        private val vcss = ServiceLoader.load(VersionControlSystem::class.java)
        private val nameToVcs = vcss.associateBy(VersionControlSystem::name)
                .filterValues(VersionControlSystem::isSupported)

        /**
         * Returns the VCS with the given `name`.
         *
         * @param name the name of the requested VCS
         * @return the requested VCS, or `null` if no such system was provided
         * or if it isn't supported in this environment
         */
        @JvmStatic fun getByName(name: String): VersionControlSystem? =
                nameToVcs[name]

        /**
         * Returns the VCS for the repository detected in the current working
         * directory.
         *
         * @return the requested VCS, or `null` if no such system could be
         * identified or if multiple systems have been identified
         */
        @JvmStatic fun get(): VersionControlSystem? = nameToVcs.values
                .filter(VersionControlSystem::detectRepository).singleOrNull()
    }

    protected object Subprocess {
        sealed class Result {
            data class Success(val input: String) : Result()
            data class Error(val message: String) : Result()
        }

        @Throws(IOException::class)
        private fun InputStream.readText(): String =
                reader().use { it.readText() }

        /**
         * Executes the given `command` and returns its resulting output.
         *
         * @param command the command which should be executed
         * @return the parsed input from the subprocess standard output
         * @throws InterruptedException if the `command` subprocess was
         * interrupted
         * @throws IOException if any input related errors occur
         */
        @Throws(InterruptedException::class, IOException::class)
        @JvmStatic fun execute(vararg command: String): Result {
            val process = ProcessBuilder().command(*command).start()
            try {
                process.outputStream.close()
                val input = process.inputStream.readText()
                val error = process.errorStream.readText()
                return if (process.waitFor() == 0) Result.Success(input)
                else Result.Error(error)
            } catch (e: InterruptedException) {
                throw SubprocessException(cause = e)
            } finally {
                process.destroy()
            }
        }
    }

    /** The name of this version control system. */
    abstract val name: String

    /**
     * Returns whether this VCS is supported in this environment.
     *
     * @throws InterruptedException if the VCS process is interrupted
     * @throws IOException if any input related errors occur
     */
    @Throws(InterruptedException::class, IOException::class)
    protected abstract fun isSupported(): Boolean

    /**
     * Returns whether a repository was detected in the current working
     * directory.
     *
     * @throws InterruptedException if the VCS process is interrupted
     * @throws IOException if any input related errors occur
     */
    @Throws(InterruptedException::class, IOException::class)
    abstract fun detectRepository(): Boolean

    /**
     * Returns the currently checked out commit.
     *
     * @return the currently checked out commit
     * @throws SubprocessException if the VCS process is interrupted or
     * terminates abnormally
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getHead(): Commit

    /**
     * Returns the commit which corresponds to the given `revision`.
     *
     * @param revision the inspected revision
     * @return the corresponding commit
     * @throws RevisionNotFoundException if `revision` doesn't exist
     * @throws SubprocessException if the VCS process is interrupted or
     * terminates abnormally
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getCommit(revision: String): Commit

    /**
     * Returns all the existing files in `revision`.
     *
     * @param revision the inspected revision
     * @return the set of files existing in the `revision`
     * @throws RevisionNotFoundException if `revision` doesn't exist
     * @throws SubprocessException if the VCS process is interrupted or
     * terminates abnormally
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun listFiles(revision: String): Set<String>

    /**
     * Returns the content of the file located at the given `path` as it is
     * found in `revision`.
     *
     * @param revision the desired revision of the file
     * @param path the relative path of the requested file
     * @return the content of the requested file, or `null` if the file doesn't
     * exist in `revision`
     * @throws RevisionNotFoundException if `revision` doesn't exist
     * @throws SubprocessException if the VCS process is interrupted or
     * terminates abnormally
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getFile(revision: String, path: String): String?

    /**
     * Returns all the commits which modified the file at the given `path`, up
     * to the given `revision`.
     *
     * The commits are given chronologically.
     *
     * @throws RevisionNotFoundException if `revision` doesn't exist
     * @throws FileNotFoundException if `path` never existed in `revision` or
     * any of its ancestors
     * @throws SubprocessException if the VCS process is interrupted or
     * terminates abnormally
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getFileHistory(revision: String, path: String): List<Commit>
}
