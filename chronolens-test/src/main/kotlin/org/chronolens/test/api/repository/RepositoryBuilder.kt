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

@file:JvmName("Builders")

package org.chronolens.test.api.repository

import java.time.Instant
import org.chronolens.api.repository.Repository
import org.chronolens.model.AddNode
import org.chronolens.model.RemoveNode
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.chronolens.model.SourceTreeEdit
import org.chronolens.model.qualifiedSourcePathOf
import org.chronolens.test.BuilderMarker
import org.chronolens.test.Init
import org.chronolens.test.api.parsing.FakeParser
import org.chronolens.test.api.repository.SourceFileChange.ChangeFile
import org.chronolens.test.api.repository.SourceFileChange.DeleteFile
import org.chronolens.test.api.repository.SourceFileChange.InvalidateFile
import org.chronolens.test.apply

@BuilderMarker
public class RepositoryBuilder {
  private val history = mutableListOf<Revision>()

  public fun revision(revision: Revision): RepositoryBuilder {
    history += revision
    return this
  }

  public operator fun Revision.unaryPlus() {
    revision(this)
  }

  public fun build(): Repository = FakeRepository(history)
}

public fun repository(init: Init<RepositoryBuilder>): Repository =
  RepositoryBuilder().apply(init).build()

public fun repository(vararg history: RevisionChangeSet): Repository {
  val repositoryBuilder = RepositoryBuilder()
  val sources = mutableMapOf<SourcePath, SourceFile>()
  val parser = FakeParser()

  fun getRemoveEdits(path: SourcePath): List<SourceTreeEdit> =
    if (path in sources) listOf(RemoveNode(qualifiedSourcePathOf(path))) else emptyList()

  fun getAddEdits(sourceFile: SourceFile): List<SourceTreeEdit> =
    getRemoveEdits(sourceFile.path) +
      listOf(AddNode(qualifiedSourcePathOf(sourceFile.path), sourceFile))

  for ((index, changeSet) in history.withIndex()) {
    val revisionId = RevisionId(index.toString())
    val edits = mutableListOf<SourceTreeEdit>()

    for (change in changeSet) {
      when (change) {
        is ChangeFile -> {
          require(parser.canParse(change.sourcePath)) {
            "Cannot parse source path '${change.sourcePath}'!"
          }
          edits += getAddEdits(change.sourceFile)
          sources[change.sourcePath] = change.sourceFile
        }
        is DeleteFile -> {
          edits += getRemoveEdits(change.sourcePath)
          sources -= change.sourcePath
        }
        is InvalidateFile -> {
          if (parser.canParse(change.sourcePath) && change.sourcePath !in sources) {
            val emptySourceFile = SourceFile(change.sourcePath)
            edits += getAddEdits(emptySourceFile)
            sources[change.sourcePath] = emptySourceFile
          }
        }
      }
    }

    val date = Instant.ofEpochMilli(1000L * index)
    val revision = Revision(revisionId, date, author = "t@test.com", edits)
    repositoryBuilder.revision(revision)
  }

  return repositoryBuilder.build()
}

public fun revisionListOf(vararg revisionIds: String): List<RevisionId> =
  revisionIds.map(::RevisionId)

public fun sourceSetOf(vararg paths: String): Set<SourcePath> = paths.map(::SourcePath).toSet()
