/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import java.util.stream.Stream
import java.io.UncheckedIOException
import kotlin.streams.asStream
import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree

/** A wrapper that connects to a repository and allows querying the source and history models. */
public interface Repository {
    /**
     * Returns the id of the `head` revision.
     *
     * @throws CorruptedRepositoryException if the repository is corrupted
     * @throws UncheckedIOException if any I/O errors occur
     */
    public fun getHeadId(): RevisionId

    /**
     * Returns the interpretable source files from the revision with the specified [revisionId].
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     * @throws CorruptedRepositoryException if the repository is corrupted
     * @throws UncheckedIOException if any I/O errors occur
     */
    public fun listSources(revisionId: RevisionId = getHeadId()): Set<SourcePath>

    /**
     * Returns the list of all revision ids in chronological order.
     *
     * @throws CorruptedRepositoryException if the repository is corrupted
     * @throws UncheckedIOException if any I/O errors occur
     */
    public fun listRevisions(): List<RevisionId>

    /**
     * Returns the source file found at the given [path] in the revision with the specified
     * [revisionId], or `null` if the [path] doesn't exist in the specified revision or couldn't be
     * interpreted.
     *
     * If the source contains syntax errors, then the most recent version which can be parsed
     * without errors will be returned. If all versions of the source contain errors, then the empty
     * source file will be returned.
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     * @throws CorruptedRepositoryException if the repository is corrupted
     * @throws UncheckedIOException if any I/O errors occur
     */
    public fun getSource(path: SourcePath, revisionId: RevisionId = getHeadId()): SourceFile?

    /**
     * Returns the snapshot of the repository at the revision with the specified [revisionId].
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     * @throws CorruptedRepositoryException if the repository is corrupted
     * @throws UncheckedIOException if any I/O errors occur
     */
    public fun getSnapshot(revisionId: RevisionId = getHeadId()): SourceTree

    /**
     * Returns a lazy view of all revisions in chronological order.
     *
     * Iterating over the sequence may throw an [UncheckedIOException] if any I/O errors
     * occur, or a [CorruptedRepositoryException] if the repository is corrupted.
     *
     * @throws CorruptedRepositoryException if the repository is corrupted
     * @throws UncheckedIOException if any I/O errors occur
     */
    public fun getHistory(): Sequence<Revision>

    /**
     * Returns the repository history and reports the progress to the given [listener]. The history
     * can be iterated through only once.
     */
    public fun getHistory(listener: HistoryProgressListener?): Sequence<Revision> {
        val history = getHistory().constrainOnce()
        listener ?: return history
        val tracker = HistoryTracker(listRevisions().size, listener)
        return history.map(tracker::onRevision)
    }

    /** Delegates to [getHistory]. The history can be iterated through only once. */
    public fun getHistoryStream(listener: HistoryProgressListener? = null): Stream<Revision> =
        getHistory(listener).asStream()

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

/**
 * Throws a [CorruptedRepositoryException] with the given [message].
 *
 * @throws CorruptedRepositoryException
 */
internal fun repositoryError(message: String): Nothing {
    throw CorruptedRepositoryException(message)
}

/**
 * Checks that the given [condition] is true.
 *
 * @throws CorruptedRepositoryException if [condition] is false
 */
internal fun checkState(condition: Boolean, lazyMessage: () -> String) {
    if (!condition) throw CorruptedRepositoryException(lazyMessage())
}

/**
 * Checks that the given revision [id] is valid.
 *
 * @throws CorruptedRepositoryException if the given [id] is invalid
 */
internal fun checkValidRevisionId(id: String): RevisionId {
    checkState(RevisionId.isValid(id)) { "Invalid revision id '$id'!" }
    return RevisionId(id)
}

/**
 * Checks that the given source file [path] is valid.
 *
 * @throws CorruptedRepositoryException if the given [path] is invalid
 */
internal fun checkValidPath(path: String): SourcePath {
    checkState(SourcePath.isValid(path)) { "Invalid source file path '$path'!" }
    return SourcePath(path)
}

/**
 * Checks that [this] list of revision ids represent a valid history.
 *
 * @throws CorruptedRepositoryException if [this] list is empty, contains duplicates or invalid
 * revision ids
 */
internal fun List<String>.checkValidHistory(): List<RevisionId> {
    checkState(isNotEmpty()) { "History must not be empty!" }
    val revisionIds = HashSet<RevisionId>(this.size)
    for (id in this.map(::checkValidRevisionId)) {
        checkState(id !in revisionIds) { "Duplicated revision id '$id'!" }
        revisionIds += id
    }
    return this.map(::RevisionId)
}
