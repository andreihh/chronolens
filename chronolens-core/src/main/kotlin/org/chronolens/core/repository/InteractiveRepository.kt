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

package org.chronolens.core.repository

import org.chronolens.api.parsing.ParseResult
import org.chronolens.api.parsing.Parser
import org.chronolens.api.repository.Repository
import org.chronolens.api.versioning.VcsProxy
import org.chronolens.api.versioning.VcsRevision
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.chronolens.model.SourceTree
import org.chronolens.model.SourceTreeEdit.Companion.apply
import org.chronolens.model.diff

/**
 * A wrapper around a repository persisted in a version control system (VCS).
 *
 * All queries retrieve and interpret the data from a [vcs] subprocess using the given [parser].
 */
internal class InteractiveRepository(private val vcs: VcsProxy, private val parser: Parser) :
  Repository {

  private fun getSourceContent(revisionId: RevisionId, path: SourcePath): String? =
    vcs.getFile(revisionId = revisionId.toString(), path = path.toString())

  private fun getChangeSet(revisionId: String): Set<SourcePath> =
    vcs.getChangeSet(revisionId = revisionId).map(::checkValidPath).toSet()

  private fun getAllSources(revisionId: RevisionId): Set<SourcePath> =
    vcs.listFiles(revisionId = revisionId.toString()).checkValidSources()

  private fun getSourceHistory(revisionId: RevisionId, path: SourcePath) =
    vcs
      .getHistory(revisionId = revisionId.toString(), path = path.toString())
      .map(VcsRevision::id)
      .map(::RevisionId)

  private fun getVcsHistory(): List<VcsRevision> = vcs.getHistory().checkValidHistory()

  override fun getHeadId(): RevisionId = vcs.getHead().id.let(::checkValidRevisionId)

  override fun listRevisions(): List<RevisionId> =
    getVcsHistory().map(VcsRevision::id).map(::RevisionId)

  override fun listSources(revisionId: RevisionId): Set<SourcePath> =
    getAllSources(revisionId).filter(parser::canParse).toSet()

  private fun parseSource(revisionId: RevisionId, path: SourcePath): ParseResult? {
    if (!parser.canParse(path)) return null
    val rawSource = getSourceContent(revisionId, path) ?: return null
    return parser.tryParse(path, rawSource)
  }

  private fun getLatestValidSource(revisionId: RevisionId, path: SourcePath): SourceFile {
    val revisions = getSourceHistory(revisionId, path).asReversed()
    for (id in revisions) {
      val result = parseSource(id, path)
      if (result is ParseResult.Success) {
        return result.source
      }
    }
    // TODO: figure out if should return null or empty file if no valid version is found.
    return SourceFile(path)
  }

  override fun getSource(path: SourcePath, revisionId: RevisionId): SourceFile? {
    return when (val result = parseSource(revisionId, path)) {
      is ParseResult.Success -> result.source
      is ParseResult.SyntaxError -> getLatestValidSource(revisionId, path)
      null -> null
    }
  }

  override fun getSnapshot(revisionId: RevisionId): SourceTree {
    val sources = listSources(revisionId).map { checkNotNull(getSource(it, revisionId)) }
    return SourceTree.of(sources)
  }

  override fun getHistory(): Sequence<Revision> {
    val sourceTree = SourceTree.empty()
    return getVcsHistory().asSequence().map { (revisionId, date, author) ->
      val changeSet = getChangeSet(revisionId)
      val before = HashSet<SourceFile>(changeSet.size)
      val after = HashSet<SourceFile>(changeSet.size)
      for (path in changeSet) {
        val oldSource = sourceTree[path]
        before += listOfNotNull(oldSource)
        val newSource =
          when (val result = parseSource(RevisionId(revisionId), path)) {
            is ParseResult.Success -> result.source
            is ParseResult.SyntaxError -> oldSource ?: SourceFile(path)
            null -> null
          }
        after += listOfNotNull(newSource)
      }
      val edits = SourceTree.of(before).diff(SourceTree.of(after))
      sourceTree.apply(edits)
      Revision(RevisionId(revisionId), date, author, edits)
    }
  }

  override fun close() {
    vcs.close()
  }
}

/**
 * Checks that [this] list of revision ids represent a valid history.
 *
 * @throws IllegalStateException if [this] list is empty, or contains invalid or duplicated revision
 * ids
 */
private fun List<VcsRevision>.checkValidHistory(): List<VcsRevision> {
  this.map(VcsRevision::id).checkValidHistory()
  return this
}

/**
 * Checks that [this] collection of source paths is valid.
 *
 * @throws IllegalStateException if [this] collection contains any invalid or duplicated source
 * paths
 */
private fun Collection<String>.checkValidSources(): Set<SourcePath> {
  val sourceFiles = LinkedHashSet<SourcePath>(this.size)
  for (source in this.map(::checkValidPath)) {
    check(source !in sourceFiles) { "Duplicated source file '$source'!" }
    sourceFiles += source
  }
  return sourceFiles
}
