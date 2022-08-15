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
import kotlin.streams.asStream
import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree

/**
 * A wrapper which connects to a repository and allows querying source code metadata and the source
 * tree history.
 */
public interface Repository {
    /**
     * Returns the id of the `head` revision.
     *
     * @throws CorruptedRepositoryException if the repository is corrupted
     */
    public fun getHeadId(): RevisionId

    /**
     * Returns the interpretable source files from the `head` revision.
     *
     * @throws CorruptedRepositoryException if the repository is corrupted
     */
    public fun listSources(): Set<SourcePath>

    /**
     * Returns the list of all revision ids in chronological order.
     *
     * @throws CorruptedRepositoryException if the repository is corrupted
     */
    public fun listRevisions(): List<RevisionId>

    /**
     * Returns the source file found at the given [path] in the `head` revision, or `null` if the
     * [path] doesn't exist in the `head` revision or couldn't be interpreted.
     *
     * If the source contains syntax errors, then the most recent version which can be parsed
     * without errors will be returned. If all versions of the source contain errors, then the empty
     * source file will be returned.
     *
     * @throws CorruptedRepositoryException if the repository is corrupted
     */
    public fun getSource(path: SourcePath): SourceFile?

    /**
     * Returns the snapshot of the repository at the `head` revision.
     *
     * @throws CorruptedRepositoryException if the repository is corrupted
     * @see getSource for details on how the latest sources are retrieved
     */
    public fun getSnapshot(): SourceTree {
        val sources = listSources().map(::getSource).checkNoNulls()
        return SourceTree.of(sources)
    }

    /**
     * Returns a lazy view of all revisions in chronological order.
     *
     * Iterating over the sequence may throw an [java.io.UncheckedIOException] if any I/O errors
     * occur, or a [CorruptedRepositoryException] if the repository is corrupted.
     *
     * @throws CorruptedRepositoryException if the repository is corrupted
     */
    public fun getHistory(): Sequence<Revision>

    /** Delegates to [getHistory]. */
    public fun getHistoryStream(): Stream<Revision> = getHistory().asStream()
}
