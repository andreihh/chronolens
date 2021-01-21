/*
 * Copyright 2018-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.SourceTreeEdit.Companion.diff
import org.chronolens.core.parsing.Parser
import org.chronolens.core.parsing.Parser.Companion.canParse
import org.chronolens.core.parsing.Result
import org.chronolens.core.versioning.Revision
import org.chronolens.core.versioning.VcsProxy
import org.chronolens.core.versioning.VcsProxyFactory
import java.io.File
import java.util.Collections.unmodifiableList
import java.util.Collections.unmodifiableSet

/**
 * A wrapper around a repository persisted in a version control system (VCS).
 *
 * All queries retrieve and interpret the data from a VCS subprocess.
 */
public class InteractiveRepository(private val vcs: VcsProxy) : Repository {
    private val head by lazy { vcs.getHead().id.let(::checkValidRevisionId) }
    private val headSources by lazy { listSources(head) }
    private val history by lazy { vcs.getHistory().let(::checkValidHistory) }

    override fun getHeadId(): String = head

    override fun listSources(): Set<String> = unmodifiableSet(headSources)

    override fun listRevisions(): List<String> =
        unmodifiableList(history.map(Revision::id))

    /**
     * Returns the interpretable source units from the revision with the
     * specified [revisionId].
     *
     * @throws IllegalArgumentException if [revisionId] is invalid or doesn't
     * exist
     * @throws CorruptedRepositoryException if the repository is corrupted
     */
    public fun listSources(revisionId: String): Set<String> {
        validateRevisionId(revisionId)
        val sources = vcs.listFiles(revisionId).filter(::canParse)
        return checkValidSources(sources)
    }

    private fun parseSource(revisionId: String, path: String): Result? {
        checkValidRevisionId(revisionId)
        checkValidPath(path)
        val rawSource = vcs.getFile(revisionId, path) ?: return null
        return Parser.parse(path, rawSource)
    }

    private fun getLatestValidSource(
        revisionId: String,
        path: String,
    ): SourceFile {
        checkValidRevisionId(revisionId)
        checkValidPath(path)
        val revisions = vcs.getHistory(path)
            .asReversed()
            .dropWhile { it.id != revisionId }
        for ((id, _, _) in revisions) {
            val result = parseSource(id, path)
            if (result is Result.Success) {
                return result.source
            }
        }
        return SourceFile(path)
    }

    /**
     * Returns the source unit found at the given [path] in the revision with
     * the specified [revisionId], or `null` if the [path] doesn't exist in the
     * specified revision or couldn't be interpreted.
     *
     * If the source contains syntax errors, then the most recent version which
     * can be parsed without errors will be returned. If all versions of the
     * source contain errors, then the empty source unit will be returned.
     *
     * @throws IllegalArgumentException if [path] or [revisionId] are invalid or
     * if [revisionId] doesn't exist
     * @throws CorruptedRepositoryException if the repository is corrupted
     */
    public fun getSource(path: String, revisionId: String): SourceFile? {
        validatePath(path)
        validateRevisionId(revisionId)
        val result = parseSource(revisionId, path)
        return when (result) {
            is Result.Success -> result.source
            Result.SyntaxError -> getLatestValidSource(revisionId, path)
            null -> null
        }
    }

    override fun getSource(path: String): SourceFile? = getSource(path, head)

    override fun getHistory(): Sequence<Transaction> {
        val sourceTree = SourceTree.empty()
        return history.asSequence().map { (revisionId, date, author) ->
            val changeSet = vcs.getChangeSet(revisionId)
            val before = HashSet<SourceFile>(changeSet.size)
            val after = HashSet<SourceFile>(changeSet.size)
            for (path in changeSet) {
                val oldSource = sourceTree.get<SourceFile?>(path)
                before += listOfNotNull(oldSource)
                val result = parseSource(revisionId, path)
                val newSource = when (result) {
                    is Result.Success -> result.source
                    Result.SyntaxError -> oldSource ?: SourceFile(path)
                    null -> null
                }
                after += listOfNotNull(newSource)
            }
            val edits = SourceTree.of(before).diff(SourceTree.of(after))
            sourceTree.apply(edits)
            Transaction(revisionId, date, author, edits)
        }
    }

    public companion object {
        /**
         * Returns the instance which can query the repository detected in the
         * given [directory] for code metadata, or `null` if no supported VCS
         * repository could be unambiguously detected
         *
         * @throws CorruptedRepositoryException if the detected repository is
         * corrupted or empty (doesn't have a `head` revision)
         */
        @JvmStatic
        public fun connect(directory: File): InteractiveRepository? =
            VcsProxyFactory.detect(directory)?.let(::InteractiveRepository)
    }
}
