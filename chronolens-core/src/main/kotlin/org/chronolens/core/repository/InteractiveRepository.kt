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

import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.SourceTreeEdit.Companion.apply
import org.chronolens.core.model.diff
import org.chronolens.core.parsing.Parser
import org.chronolens.core.parsing.Result
import org.chronolens.core.versioning.VcsProxy
import org.chronolens.core.versioning.VcsRevision

/**
 * A wrapper around a repository persisted in a version control system (VCS).
 *
 * All queries retrieve and interpret the data from a [vcs] subprocess using the given [parser].
 */
internal class InteractiveRepository(
    private val vcs: VcsProxy,
    private val parser: Parser
) : Repository {

    private val head by lazy { tryRun { vcs.getHead() }.id.let(::checkValidRevisionId) }
    private val history by lazy { tryRun { vcs.getHistory() }.checkValidHistory() }

    private fun getSourceContent(revisionId: RevisionId, path: SourcePath): String? =
        tryRun { vcs.getFile(revisionId.toString(), path.toString()) }

    private fun getChangeSet(revisionId: String): Set<SourcePath> =
        tryRun { vcs.getChangeSet(revisionId) }.map(::checkValidPath).toSet()

    private fun getAllSources(revisionId: RevisionId): Set<SourcePath> =
        tryRun { vcs.listFiles(revisionId.toString()) }.checkValidSources()

    private fun getSourceHistory(path: SourcePath) =
        tryRun { vcs.getHistory(path.toString()) }.map(VcsRevision::id).map(::RevisionId)

    override fun getHeadId(): RevisionId = head

    override fun listRevisions(): List<RevisionId> = history.map(VcsRevision::id).map(::RevisionId)

    override fun listSources(revisionId: RevisionId): Set<SourcePath> =
        getAllSources(revisionId).filter(parser::canParse).toSet()

    private fun parseSource(revisionId: RevisionId, path: SourcePath): Result? {
        if (!parser.canParse(path)) return null
        val rawSource = getSourceContent(revisionId, path) ?: return null
        return parser.tryParse(path, rawSource)
    }

    private fun getLatestValidSource(revisionId: RevisionId, path: SourcePath): SourceFile {
        val revisions = getSourceHistory(path).asReversed().dropWhile { it != revisionId }
        for (id in revisions) {
            val result = parseSource(id, path)
            if (result is Result.Success) {
                return result.source
            }
        }
        // TODO: figure out if should return null or empty file if no valid version is found.
        return SourceFile(path)
    }

    override fun getSource(path: SourcePath, revisionId: RevisionId): SourceFile? {
        return when (val result = parseSource(revisionId, path)) {
            is Result.Success -> result.source
            Result.SyntaxError -> getLatestValidSource(revisionId, path)
            null -> null
        }
    }

    override fun getSnapshot(revisionId: RevisionId): SourceTree {
        val sources = listSources(revisionId).map { getSource(it, revisionId) }.checkNoNulls()
        return SourceTree.of(sources)
    }

    override fun getHistory(): Sequence<Revision> {
        val sourceTree = SourceTree.empty()
        return history.asSequence().map { (revisionId, date, author) ->
            val changeSet = getChangeSet(revisionId)
            val before = HashSet<SourceFile>(changeSet.size)
            val after = HashSet<SourceFile>(changeSet.size)
            for (path in changeSet) {
                val oldSource = sourceTree[path]
                before += listOfNotNull(oldSource)
                val newSource =
                    when (val result = parseSource(RevisionId(revisionId), path)) {
                        is Result.Success -> result.source
                        Result.SyntaxError -> oldSource ?: SourceFile(path)
                        null -> null
                    }
                after += listOfNotNull(newSource)
            }
            val edits = SourceTree.of(before).diff(SourceTree.of(after))
            sourceTree.apply(edits)
            Revision(RevisionId(revisionId), date, author, edits)
        }
    }
}

/**
 * Checks that [this] list of revision ids represent a valid history.
 *
 * @throws CorruptedRepositoryException if [this] list is empty, or contains invalid or duplicated
 * revision ids
 */
private fun List<VcsRevision>.checkValidHistory(): List<VcsRevision> {
    this.map(VcsRevision::id).checkValidHistory()
    return this
}

/**
 * Checks that [this] collection of source paths is valid.
 *
 * @throws CorruptedRepositoryException if [this] collection contains any invalid or duplicated
 * source paths
 */
private fun Collection<String>.checkValidSources(): Set<SourcePath> {
    val sourceFiles = LinkedHashSet<SourcePath>(this.size)
    for (source in this.map(::checkValidPath)) {
        checkState(source !in sourceFiles) { "Duplicated source file '$source'!" }
        sourceFiles += source
    }
    return sourceFiles
}

/**
 * Checks that [this] collection doesn't contain any `null` elements.
 *
 * @throws CorruptedRepositoryException if any element is `null`
 */
private fun <T : Any> Collection<T?>.checkNoNulls(): Collection<T> {
    for (item in this) {
        if (item == null) {
            throw CorruptedRepositoryException("'null' found in '$this'!")
        }
    }
    @Suppress("UNCHECKED_CAST") return this as Collection<T>
}

private fun <T> tryRun(block: () -> T): T = try {
    block()
} catch (e: IllegalStateException) {
    throw CorruptedRepositoryException(e)
}
