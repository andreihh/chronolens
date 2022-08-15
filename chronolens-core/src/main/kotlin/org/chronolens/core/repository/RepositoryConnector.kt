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

import org.chronolens.core.repository.RepositoryConnector.AccessMode.ANY
import org.chronolens.core.repository.RepositoryConnector.AccessMode.FAST_HISTORY
import org.chronolens.core.repository.RepositoryConnector.AccessMode.RANDOM_ACCESS
import org.chronolens.core.versioning.VcsProxyFactory
import java.io.File

public object RepositoryConnector {
    /** Specifies the mode in which the [Repository] is accessed. */
    public enum class AccessMode {
        /** No specific requirements. */
        ANY,

        /** Requires fast access to [Repository.getSource]. */
        RANDOM_ACCESS,

        /** Requires fast access to [Repository.getHistory]. */
        FAST_HISTORY,
    }

    /**
     * Returns the [Repository] detected in the given [rootDirectory] that supports the specified
     * [accessMode], or `null` if no supported repository could be unambiguously detected.
     *
     * @throws CorruptedRepositoryException if the detected repository is corrupted
     */
    @JvmStatic
    public fun tryConnect(accessMode: AccessMode, rootDirectory: File): Repository? =
        when (accessMode) {
            RANDOM_ACCESS -> tryConnectInteractive(rootDirectory)
            FAST_HISTORY -> tryLoadPersistent(rootDirectory)
            ANY -> tryConnectInteractive(rootDirectory) ?: tryLoadPersistent(rootDirectory)
        }

    /**
     * Returns the [Repository] detected in the given [rootDirectory] that supports the specified
     * [accessMode].
     *
     * @throws CorruptedRepositoryException if the detected repository is corrupted or if no
     * supported repository could be unambiguously detected
     */
    @JvmStatic
    public fun connect(accessMode: AccessMode, rootDirectory: File): Repository =
        tryConnect(accessMode, rootDirectory)
            ?: repositoryError("No repository found in '$rootDirectory' for mode '$accessMode'!")

    private fun tryConnectInteractive(rootDirectory: File): Repository? =
        VcsProxyFactory.detect(rootDirectory)?.let(::InteractiveRepository)

    private fun tryLoadPersistent(rootDirectory: File): Repository? {
        val schema = RepositoryFileSchema(rootDirectory)
        return if (!schema.rootDirectory.isDirectory) null else PersistentRepository(schema)
    }
}
