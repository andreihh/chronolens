/*
 * Copyright 2022-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import org.chronolens.api.database.RepositoryDatabase
import org.chronolens.api.parsing.Parser
import org.chronolens.api.repository.Repository
import org.chronolens.api.repository.Repository.AccessMode
import org.chronolens.api.repository.Repository.AccessMode.ANY
import org.chronolens.api.repository.Repository.AccessMode.FAST_HISTORY
import org.chronolens.api.repository.Repository.AccessMode.RANDOM_ACCESS
import org.chronolens.api.versioning.VcsProxy
import org.chronolens.api.versioning.VcsProxyFactory
import org.chronolens.core.repository.RepositoryFileStorage.Companion.STORAGE_ROOT_DIRECTORY

public class RepositoryConnector private constructor(private val rootDirectory: File) {
  /**
   * Returns the [Repository] detected in the given [rootDirectory] that supports the specified
   * [accessMode], or `null` if no supported repository could be unambiguously detected.
   *
   * @throws IllegalStateException if the detected repository is corrupted
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
   * @throws IllegalStateException if the detected repository is corrupted or if no supported
   * repository could be unambiguously detected
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun connect(accessMode: AccessMode): Repository =
    tryConnect(accessMode)
      ?: error("No repository found in '$rootDirectory' for mode '$accessMode'!")

  public fun tryOpen(): RepositoryDatabase? =
    try {
      if (!File(rootDirectory, STORAGE_ROOT_DIRECTORY).isDirectory) null
      else RepositoryFileStorage(rootDirectory)
    } catch (e: IOException) {
      throw UncheckedIOException(e)
    }

  public fun open(): RepositoryDatabase =
    tryOpen() ?: ioError("No repository storage found in '$rootDirectory'!")

  public fun openOrCreate(): RepositoryDatabase = RepositoryFileStorage(rootDirectory)

  /**
   * Deletes the repository storage from the given [rootDirectory].
   *
   * All corresponding [RepositoryDatabase] and [PersistentRepository] instances will become
   * corrupted after this method is called.
   *
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun delete() {
    if (!File(rootDirectory, STORAGE_ROOT_DIRECTORY).deleteRecursively()) {
      ioError("Failed to delete '$rootDirectory' recursively!")
    }
  }

  private fun tryConnectVcs(): VcsProxy? = VcsProxyFactory.connect(rootDirectory)

  private fun tryConnectInteractive(): Repository? =
    tryConnectVcs()?.let { vcsProxy -> InteractiveRepository(vcsProxy, Parser.Registry) }

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
