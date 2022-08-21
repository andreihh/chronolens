/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.repository

import org.chronolens.core.parsing.Parser
import org.chronolens.core.repository.RepositoryConnector.AccessMode.ANY
import org.chronolens.core.repository.RepositoryConnector.AccessMode.FAST_HISTORY
import org.chronolens.core.repository.RepositoryConnector.AccessMode.RANDOM_ACCESS
import org.chronolens.core.repository.RepositoryFileStorage.Companion.STORAGE_ROOT_DIRECTORY
import org.chronolens.core.versioning.VcsProxy
import org.chronolens.core.versioning.VcsProxyFactory
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException

public class RepositoryConnector private constructor(private val rootDirectory: File) {
    /** Specifies the mode in which the [Repository] is accessed. */
    public enum class AccessMode {
        /** No specific requirements. */
        ANY,

        /** Requires fast access to [Repository.getSnapshot] and [Repository.getSource]. */
        RANDOM_ACCESS,

        /** Requires fast access to [Repository.getHistory] and [Repository.listRevisions]. */
        FAST_HISTORY,
    }

    /**
     * Returns the [Repository] detected in the given [rootDirectory] that supports the specified
     * [accessMode], or `null` if no supported repository could be unambiguously detected.
     *
     * @throws CorruptedRepositoryException if the detected repository is corrupted
     * @throws UncheckedIOException if any I/O errors occur
     */
    public fun tryConnect(accessMode: AccessMode): Repository? =
        when (accessMode) {
            RANDOM_ACCESS -> tryConnectInteractive()
            FAST_HISTORY -> tryLoadPersistent()
            ANY -> tryConnectInteractive() ?: tryLoadPersistent()
        }

    /**
     * Returns the [Repository] detected in the given [rootDirectory] that supports the specified
     * [accessMode].
     *
     * @throws CorruptedRepositoryException if the detected repository is corrupted or if no
     * supported repository could be unambiguously detected
     * @throws UncheckedIOException if any I/O errors occur
     */
    public fun connect(accessMode: AccessMode): Repository =
        tryConnect(accessMode)
            ?: repositoryError("No repository found in '$rootDirectory' for mode '$accessMode'!")

    public fun tryOpen(): RepositoryStorage? =
        try {
            if (!File(rootDirectory, STORAGE_ROOT_DIRECTORY).isDirectory) null
            else RepositoryFileStorage(rootDirectory)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    public fun open(): RepositoryStorage =
        tryOpen() ?: ioError("No repository storage found in '$rootDirectory'!")

    public fun openOrCreate(): RepositoryStorage = RepositoryFileStorage(rootDirectory)

    /**
     * Deletes the repository storage from the given [rootDirectory].
     *
     * All corresponding [RepositoryStorage] and [PersistentRepository] instances will become
     * corrupted after this method is called.
     *
     * @throws UncheckedIOException if any I/O errors occur
     */
    public fun delete() {
        if (!File(rootDirectory, STORAGE_ROOT_DIRECTORY).deleteRecursively()) {
            ioError("Failed to delete '$rootDirectory' recursively!")
        }
    }

    private fun tryConnectVcs(): VcsProxy? = VcsProxyFactory.detect(rootDirectory)

    private fun tryConnectInteractive(): Repository? = tryConnectVcs()?.let { vcsProxy ->
        InteractiveRepository(vcsProxy, Parser.Registry)
    }

    private fun tryLoadPersistent(): Repository? =
        try {
            tryOpen()?.let(::PersistentRepository)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    public companion object {
        @JvmStatic
        public fun newConnector(rootDirectory: File): RepositoryConnector =
            RepositoryConnector(rootDirectory)
    }
}

private fun ioError(message: String, cause: Throwable? = null): Nothing {
    throw UncheckedIOException(IOException(message, cause))
}
