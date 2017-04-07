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

import org.metanalysis.core.versioning.SubprocessException.SubprocessInterruptedException
import org.metanalysis.core.versioning.SubprocessException.SubprocessTerminatedException

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
        private val vcsList by lazy {
            ServiceLoader.load(VersionControlSystem::class.java)
                    .filter(VersionControlSystem::isSupported)
        }

        /**
         * Returns the VCS for the repository detected in the current working
         * directory.
         *
         * @return the requested VCS, or `null` if no supported VCS repository
         * was detected or if multiple repositories were detected
         * @throws SubprocessInterruptedException if the VCS process is
         * interrupted
         * @throws IOException if the configuration file couldn't be parsed or
         * if any input related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic fun get(): VersionControlSystem? =
                vcsList.filter(VersionControlSystem::detectRepository)
                        .singleOrNull()
    }

    protected object Subprocess {
        sealed class Result {
            data class Success(val input: String) : Result()
            data class Error(val exitCode: Int, val message: String) : Result()
        }

        @Throws(IOException::class)
        private fun InputStream.readText(): String =
                reader().use { it.readText() }

        /**
         * Executes the given `command` in a subprocess and returns its result.
         *
         * @param command the command which should be executed
         * @return the parsed input from `stdout` (if the subprocess terminated
         * normally) or from `stderr` (if the subprocess terminated abnormally)
         * @throws SubprocessInterruptedException if the subprocess was
         * interrupted
         * @throws IOException if any input related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic fun execute(vararg command: String): Result {
            val process = ProcessBuilder().command(*command).start()
            try {
                process.outputStream.close()
                val input = process.inputStream.readText()
                val error = process.errorStream.readText()
                val exitCode = process.waitFor()
                return if (exitCode == 0) Result.Success(input)
                else Result.Error(exitCode, error)
            } catch (e: InterruptedException) {
                throw SubprocessInterruptedException(cause = e)
            } finally {
                process.destroy()
            }
        }
    }

    /**
     * Returns whether this VCS is supported in this environment.
     *
     * @throws SubprocessInterruptedException if the VCS process is interrupted
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    protected abstract fun isSupported(): Boolean

    /**
     * Returns whether a repository was detected in the current working
     * directory.
     *
     * @throws SubprocessInterruptedException if the VCS process is interrupted
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun detectRepository(): Boolean

    /**
     * Returns the `head` commit.
     *
     * @return the `head` commit
     * @throws SubprocessInterruptedException if the VCS process is interrupted
     * @throws SubprocessTerminatedException if the VCS process terminates
     * abnormally
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getHead(): Commit

    /**
     * Returns the commit which corresponds to the given `revision`.
     *
     * @param revision the inspected revision
     * @return the corresponding commit
     * @throws SubprocessInterruptedException if the VCS process is interrupted
     * @throws SubprocessTerminatedException if the VCS process terminates
     * abnormally
     * @throws ObjectNotFoundException if `revision` doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getCommit(revision: String): Commit

    /**
     * Returns all the existing files in `revision`.
     *
     * @param revision the inspected revision
     * @return the set of files existing in the `revision`
     * @throws SubprocessInterruptedException if the VCS process is interrupted
     * @throws SubprocessTerminatedException if the VCS process terminates
     * abnormally
     * @throws ObjectNotFoundException if `revision` doesn't exist
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
     * @return the content of the requested file
     * @throws SubprocessInterruptedException if the VCS process is interrupted
     * @throws SubprocessTerminatedException if the VCS process terminates
     * abnormally
     * @throws ObjectNotFoundException if `revision` doesn't exist or if `path`
     * doesn't exist in `revision`
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
     * @throws SubprocessInterruptedException if the VCS process is interrupted
     * @throws SubprocessTerminatedException if the VCS process terminates
     * abnormally
     * @throws ObjectNotFoundException if `revision` doesn't exist or if `path`
     * never existed in `revision` or any of its ancestors
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getFileHistory(revision: String, path: String): List<Commit>
}
