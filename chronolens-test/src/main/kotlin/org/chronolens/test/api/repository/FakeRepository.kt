/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.api.repository

import org.chronolens.api.repository.Repository
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.chronolens.model.SourceTree
import org.chronolens.model.SourceTreeEdit.Companion.apply

internal class FakeRepository(revisions: List<Revision>) : Repository {
  private val history by lazy {
    check(revisions.isNotEmpty()) { "History must not be empty!" }
    revisions
  }
  private var closed = false

  private val snapshots by lazy {
    val snapshots = mutableMapOf<RevisionId, SourceTree>()
    val snapshot = SourceTree.empty()
    for (revision in history) {
      snapshot.apply(revision.edits)
      snapshots[revision.id] = SourceTree.of(snapshot.sources)
    }
    snapshots
  }

  override fun getHeadId(): RevisionId = runIfNotClosedOrThrow { history.last().id }

  override fun listSources(revisionId: RevisionId): Set<SourcePath> = runIfNotClosedOrThrow {
    requireNotNull(snapshots[revisionId]).sources.map(SourceFile::path).toSet()
  }

  override fun listRevisions(): List<RevisionId> = runIfNotClosedOrThrow {
    history.map(Revision::id)
  }

  override fun getSource(path: SourcePath, revisionId: RevisionId): SourceFile? =
    runIfNotClosedOrThrow {
      requireNotNull(snapshots[revisionId])[path]
    }

  override fun getSnapshot(revisionId: RevisionId): SourceTree = runIfNotClosedOrThrow {
    requireNotNull(snapshots[revisionId])
  }

  override fun getHistory(): Sequence<Revision> = runIfNotClosedOrThrow { history.asSequence() }

  override fun close() {
    closed = true
  }

  private fun <T> runIfNotClosedOrThrow(block: () -> T): T {
    check(!closed) { "Repository was already closed!" }
    return block()
  }
}
