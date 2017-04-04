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
 * An abstract version control system which interacts with the repository found
 * in the current working directory.
 *
 * Version control systems must have a public no-arg constructor.
 *
 * The file
 * `META-INF/services/org.metanalysis.core.versioning.VersionControlSystem` must
 * be provided and must contain the list of all provided version control
 * systems.
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
        protected abstract val command: List<String>

        @Throws(IOException::class)
        protected abstract fun onSuccess(input: String): T

        @Throws(IOException::class)
        protected open fun onError(error: String): Nothing =
                throw IOException(error)

        @Throws(IOException::class)
        protected fun InputStream.readText(): String =
                readBytes().toString(charset("UTF-8"))

        @Throws(IOException::class)
        private fun Process.close() {
            inputStream.close()
            errorStream.close()
            outputStream.close()
        }

        @Throws(IOException::class)
        fun run(): T {
            val process = ProcessBuilder().command(command).start()
            return try {
                if (process.waitFor() == 0)
                    onSuccess(process.inputStream.readText())
                else onError(process.errorStream.readText())
            } catch (e: InterruptedException) {
                throw IOException(e)
            } finally {
                process.close()
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
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getHead(): Commit

    /**
     * @throws IllegalArgumentException if the given `revisionId` is invalid or
     * doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getCommit(revisionId: String): Commit

    /**
     *
     * @throws IllegalArgumentException if the given `revisionId` is invalid or
     * doesn't exist
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun listFiles(revisionId: String): Set<String>

    /**
     *
     * @return `null` if the given `path` doesn't exist in the given `revId`
     * @throws IllegalArgumentException if the given `revisionId` is invalid or
     * doesn't exist
     * @throws FileNotFoundException if the given `path` doesn't exist in the
     * given `revisionId`
     * @throws IOException if any input related errors occur
     */
    @Throws(FileNotFoundException::class, IOException::class)
    abstract fun getFile(revisionId: String, path: String): String

    /**
     *
     * @throws IllegalArgumentException if the given `revisionId` is invalid or
     * doesn't exist
     * @throws FileNotFoundException if the given `path` never existed in the
     * given `revisionId` or any of its ancestor revisions
     * @throws IOException if any input related errors occur
     */
    @Throws(FileNotFoundException::class, IOException::class)
    abstract fun getFileHistory(revisionId: String, path: String): List<Commit>
}
