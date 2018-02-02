/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.metanalysis.core.repository

import org.metanalysis.core.model.Project
import org.metanalysis.core.model.ProjectEdit.Companion.diff
import org.metanalysis.core.model.SourceUnit
import org.metanalysis.core.parsing.Parser
import org.metanalysis.core.parsing.Result
import org.metanalysis.core.parsing.SourceFile
import org.metanalysis.core.versioning.Revision
import org.metanalysis.core.versioning.VcsProxy
import org.metanalysis.core.versioning.VcsProxyFactory
import java.util.Collections.unmodifiableList
import java.util.Collections.unmodifiableSet

/**
 * A wrapper around a repository persisted in a version control system (VCS).
 *
 * All queries retrieve and interpret the data from a VCS subprocess.
 */
class InteractiveRepository private constructor(private val vcs: VcsProxy) :
    Repository {

    private val headId = vcs.getHead().id.apply(::checkValidTransactionId)
    private val headSources = listSources(headId)
    private val history = vcs.getHistory().apply(::checkValidHistory)

    private fun canParse(path: String): Boolean = Parser.getParser(path) != null

    override fun getHeadId(): String = headId

    /**
     * Returns the set of source units which can be interpreted from the
     * revision with the specified [revisionId].
     *
     * @throws IllegalArgumentException if the given [revisionId] is invalid
     * @throws IllegalStateException if this repository is in a corrupted state
     */
    fun listSources(revisionId: String): Set<String> {
        validateTransactionId(revisionId)
        val sources = vcs.listFiles(revisionId).filter(::canParse).toSet()
        sources.forEach(::checkValidPath)
        return unmodifiableSet(sources)
    }

    override fun listSources(): Set<String> = headSources

    override fun listRevisions(): List<String> =
        unmodifiableList(history.map(Revision::id))

    private fun parseSource(revisionId: String, path: String): Result? {
        checkValidTransactionId(revisionId)
        checkValidPath(path)
        val source = vcs.getFile(revisionId, path) ?: return null
        return Parser.parse(SourceFile(path, source))
    }

    private fun getLatestValidSource(
        revisionId: String,
        path: String
    ): SourceUnit {
        checkValidTransactionId(revisionId)
        checkValidPath(path)
        val revisions = vcs.getHistory(path)
            .asReversed()
            .dropWhile { it.id != revisionId }
        for ((id, _, _) in revisions) {
            val result = parseSource(id, path)
            if (result is Result.Success) {
                return result.sourceUnit
            }
        }
        return SourceUnit(path)
    }

    /**
     * Returns the source unit found at the given [path] as it is found in the
     * revision with the specified [revisionId], or `null` if the [path] doesn't
     * exist in the specified revision or couldn't be interpreted.
     *
     * If the source contains syntax errors, then the most recent version which
     * can be parsed without errors will be returned. If all versions of the
     * source contain errors, then the empty source unit will be returned.
     *
     * @throws IllegalArgumentException if [path] or [revisionId] are invalid
     * @throws IllegalStateException if this repository is in a corrupted state
     */
    fun getSource(path: String, revisionId: String): SourceUnit? {
        validatePath(path)
        validateTransactionId(revisionId)
        val result = parseSource(revisionId, path)
        return when (result) {
            is Result.Success -> result.sourceUnit
            Result.SyntaxError -> getLatestValidSource(revisionId, path)
            null -> null
        }
    }

    override fun getSource(path: String): SourceUnit? = getSource(path, headId)

    override fun getHistory(): Iterable<Transaction> {
        val project = Project.empty()
        return history.mapLazy { (revisionId, date, author) ->
            val changeSet = vcs.getChangeSet(revisionId)
            val before = hashSetOf<SourceUnit>()
            val after = hashSetOf<SourceUnit>()
            for (path in changeSet) {
                val oldUnit = project.get<SourceUnit?>(path)
                if (oldUnit != null) {
                    before += oldUnit
                }
                val result = parseSource(revisionId, path)
                val newUnit = when (result) {
                    is Result.Success -> result.sourceUnit
                    Result.SyntaxError -> oldUnit ?: SourceUnit(path)
                    null -> null
                }
                if (newUnit != null) {
                    after += newUnit
                }
            }
            val edits = Project.of(before).diff(Project.of(after))
            project.apply(edits)
            Transaction(revisionId, date, author, edits)
        }
    }

    companion object {
        /**
         * Returns the instance which can query the repository detected in the
         * current working directory for code metadata, or `null` if no
         * supported VCS repository could be unambiguously detected
         *
         * @throws IllegalStateException if the detected repository is corrupted
         * or empty (doesn't have a `head` revision)
         */
        @JvmStatic
        fun connect(): InteractiveRepository? =
            VcsProxyFactory.detect()?.let(::InteractiveRepository)
    }
}
