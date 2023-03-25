/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import java.util.stream.Stream
import kotlin.streams.asStream
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.chronolens.model.SourceTree

/** A wrapper that connects to a repository and allows querying the source and history models. */
public interface Repository : AutoCloseable {
  /** Specifies access requirements for connecting to a [Repository]. */
  public enum class AccessMode {
    /** No specific requirements. */
    ANY,

    /** Requires fast access to [Repository.getSnapshot] and [Repository.getSource]. */
    RANDOM_ACCESS,

    /** Requires fast access to [Repository.getHistory] and [Repository.listRevisions]. */
    FAST_HISTORY,
  }

  /**
   * Returns the id of the `head` revision.
   *
   * @throws IllegalStateException if the repository is corrupted
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun getHeadId(): RevisionId

  /**
   * Returns the interpretable source files from the revision with the specified [revisionId].
   *
   * @throws IllegalArgumentException if [revisionId] doesn't exist
   * @throws IllegalStateException if the repository is corrupted
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun listSources(revisionId: RevisionId = getHeadId()): Set<SourcePath>

  /**
   * Returns the list of all revision ids in chronological order.
   *
   * @throws IllegalStateException if the repository is corrupted
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun listRevisions(): List<RevisionId>

  /**
   * Returns the source file found at the given [path] in the revision with the specified
   * [revisionId], or `null` if the [path] doesn't exist in the specified revision or couldn't be
   * interpreted.
   *
   * If the source contains syntax errors, then the most recent version which can be parsed without
   * errors will be returned. If all versions of the source contain errors, then the empty source
   * file will be returned.
   *
   * @throws IllegalArgumentException if [revisionId] doesn't exist
   * @throws IllegalStateException if the repository is corrupted
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun getSource(path: SourcePath, revisionId: RevisionId = getHeadId()): SourceFile?

  /**
   * Returns the snapshot of the repository at the revision with the specified [revisionId].
   *
   * @throws IllegalArgumentException if [revisionId] doesn't exist
   * @throws IllegalStateException if the repository is corrupted
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun getSnapshot(revisionId: RevisionId = getHeadId()): SourceTree

  /**
   * Returns a lazy view of all revisions in chronological order.
   *
   * Iterating over the sequence may throw an [IllegalStateException] if the repository is
   * corrupted, or an [UncheckedIOException] if any I/O errors occur.
   *
   * @throws IllegalStateException if the repository is corrupted
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun getHistory(): Sequence<Revision>

  /**
   * Returns the repository history and reports the progress to the given [listener]. The history
   * can be iterated through only once.
   *
   * Iterating over the sequence may throw an [IllegalStateException] if the repository is
   * corrupted, or an [UncheckedIOException] if any I/O errors occur.
   *
   * @throws IllegalStateException if the repository is corrupted
   * @throws UncheckedIOException if any I/O errors occur
   */
  public fun getHistory(listener: HistoryProgressListener?): Sequence<Revision> {
    val history = getHistory().constrainOnce()
    listener ?: return history
    val tracker = HistoryTracker(listRevisions().size, listener)
    return history.map(tracker::onRevision)
  }

  /** Delegates to [getHistory]. */
  public fun getHistoryStream(listener: HistoryProgressListener? = null): Stream<Revision> =
    getHistory(listener).asStream()

  /**
   * Closes this repository, relinquishing any underlying resources.
   *
   * This method is idempotent. Calling any other method after the repository was closed may fail
   * with an [IllegalStateException].
   *
   * @throws IllegalStateException if the repository is corrupted and couldn't be closed
   * @throws UncheckedIOException if any I/O errors occur
   */
  override fun close()

  /** A listener notified on the progress of iterating through a repository's history. */
  public interface HistoryProgressListener {
    public fun onStart(revisionCount: Int)
    public fun onRevision(revision: Revision)
    public fun onEnd()
  }

  private class HistoryTracker(
    private val revisionCount: Int,
    private val listener: HistoryProgressListener,
  ) {

    private var trackedRevisions = 0

    fun onRevision(revision: Revision): Revision {
      if (trackedRevisions == 0) {
        listener.onStart(revisionCount)
      }
      listener.onRevision(revision)
      trackedRevisions++
      if (trackedRevisions == revisionCount) {
        listener.onEnd()
      }
      return revision
    }
  }
}
