/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.api.repository

import java.io.UncheckedIOException

public interface RepositoryConnector {
  /**
   * Returns the [Repository] identified by the given [repositoryId] that supports the specified
   * [accessMode], or `null` if no supported repository could be unambiguously detected.
   *
   * @throws CorruptedRepositoryException if the detected repository is corrupted
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun tryConnect(repositoryId: RepositoryId, accessMode: Repository.AccessMode): Repository?

  /**
   * Returns the [Repository] identified by the given [repositoryId] that supports the specified
   * [accessMode].
   *
   * @throws CorruptedRepositoryException if the detected repository is corrupted or if no supported
   * repository could be unambiguously detected
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun connect(repositoryId: RepositoryId, accessMode: Repository.AccessMode): Repository =
    tryConnect(repositoryId, accessMode)
      ?: repositoryError("Could not connect to repository '$repositoryId' in mode '$accessMode'!")
}
