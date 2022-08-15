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

import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath

/**
 * A wrapper that connects to a repository with its full history and allows efficient queries for
 * the source code metadata at any point in the history.
 */
public interface InteractiveRepository : Repository {

    /**
     * Returns the interpretable source units from the revision with the specified [revisionId].
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     * @throws CorruptedRepositoryException if the repository is corrupted
     */
    public fun listSources(revisionId: RevisionId): Set<SourcePath>

    override fun listSources(): Set<SourcePath> = listSources(getHeadId())

    /**
     * Returns the source unit found at the given [path] in the revision with the specified
     * [revisionId], or `null` if the [path] doesn't exist in the specified revision or couldn't be
     * interpreted.
     *
     * If the source contains syntax errors, then the most recent version which can be parsed
     * without errors will be returned. If all versions of the source contain errors, then the empty
     * source unit will be returned.
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     * @throws CorruptedRepositoryException if the repository is corrupted
     */
    public fun getSource(path: SourcePath, revisionId: RevisionId): SourceFile?

    override fun getSource(path: SourcePath): SourceFile? = getSource(path, getHeadId())
}
