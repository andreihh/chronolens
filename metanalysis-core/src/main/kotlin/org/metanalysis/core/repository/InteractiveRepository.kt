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
import org.metanalysis.core.model.SourceNode.SourceUnit
import org.metanalysis.core.parsing.Parser
import org.metanalysis.core.parsing.Result
import org.metanalysis.core.versioning.VcsProxy
import org.metanalysis.core.versioning.VcsProxyFactory

/**
 * A wrapper around a repository persisted in a version control system (VCS).
 *
 * All queries retrieve and interpret the data from a VCS subprocess.
 */
class InteractiveRepository private constructor(
        private val vcs: VcsProxy
): Repository {
    companion object {
        /**
         * Returns the instance which can query the repository detected in the
         * current working directory for code metadata.
         *
         * @return the repository instance, or `null` if no supported VCS
         * repository could be unambiguously detected
         * @throws IllegalStateException if the detected repository is corrupted
         * or empty (doesn't have a `head` revision)
         */
        @JvmStatic
        fun connect(): InteractiveRepository? =
                VcsProxyFactory.detect()?.let(::InteractiveRepository)
    }

    private val String.isParsable: Boolean
        get() = Parser.getParser(this) != null

    override fun getHeadId(): String = vcs.getHead().id

    private fun listSources(revisionId: String): Set<String> =
            vcs.listFiles(revisionId).filter { it.isParsable }.toSet()

    override fun listSources(): Set<String> = listSources(getHeadId())

    private fun parseSourceUnit(revisionId: String, path: String): Result? {
        val source = vcs.getFile(revisionId, path) ?: return null
        return Parser.parse(path, source)
    }

    private fun getLatestValidSourceUnit(path: String): SourceUnit {
        val revisions = vcs.getHistory(path).asReversed()
        for ((revisionId, _, _) in revisions) {
            val result = parseSourceUnit(revisionId, path)
            if (result is Result.Success) {
                return result.sourceUnit
            }
        }
        return SourceUnit(path)
    }

    override fun getSourceUnit(path: String): SourceUnit? {
        val result = parseSourceUnit(getHeadId(), path)
        return when (result) {
            is Result.Success -> result.sourceUnit
            Result.SyntaxError -> getLatestValidSourceUnit(path)
            null -> null
        }
    }

    override fun getHistory(): Iterable<Transaction> {
        val project = Project.empty()
        return vcs.getHistory().asSequence().map { (revisionId, date, author) ->
            val changeSet = vcs.getChangeSet(revisionId)
            val before = hashSetOf<SourceUnit>()
            val after = hashSetOf<SourceUnit>()
            for (path in changeSet) {
                val oldUnit = project.find<SourceUnit>(path)
                if (oldUnit != null) {
                    before += oldUnit
                }
                val result = parseSourceUnit(revisionId, path)
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
        }.asIterable()
    }
}
