/*
 * Copyright 2017-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.api.versioning

import java.io.UncheckedIOException

/** A version control system (VCS) proxy which interacts with a connected repository. */
public interface VcsProxy : AutoCloseable {
  /**
   * Returns the `head` revision.
   *
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun getHead(): VcsRevision

  /**
   * Returns the revision with the given [revisionId], or `null` if no such revision exists.
   *
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun getRevision(revisionId: String): VcsRevision?

  /**
   * Returns the files inside the current working directory which were modified in the revision with
   * the given [revisionId].
   *
   * @throws IllegalArgumentException if [revisionId] doesn't exist
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun getChangeSet(revisionId: String): Set<String>

  /**
   * Returns the set of existing files in the current working directory in the revision with the
   * given [revisionId].
   *
   * @throws IllegalArgumentException if [revisionId] doesn't exist
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun listFiles(revisionId: String): Set<String>

  /**
   * Returns the content of the file located at the given relative [path] as it is found in the
   * revision with the given [revisionId], or `null` if it doesn't exist in the specified revision.
   *
   * @throws IllegalArgumentException if [revisionId] doesn't exist
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun getFile(revisionId: String, path: String): String?

  /**
   * Returns the chronological list of revisions which modified the file or directory at the given
   * [path] up to and including the given [revisionId], or the empty list if [path] never existed in
   * the given revision or any of its ancestors.
   *
   * A directory is modified if any file in its subtree is modified. The empty string is a path that
   * represents the repository root.
   *
   * @throws IllegalArgumentException if [revisionId] doesn't exist
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun getHistory(revisionId: String = getHead().id, path: String = ""): List<VcsRevision>

  /**
   * Closes this VCS proxy, relinquishing any underlying resources.
   *
   * This method is idempotent. Calling any other method after the VCS proxy was closed may fail
   * with an [IllegalStateException].
   *
   * @throws UncheckedIOException if any I/O errors occur
   */
  override fun close()
}
