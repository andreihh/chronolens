/*
 * Copyright 2017-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.api.repository.Repository
import org.chronolens.test.api.parsing.FakeParser
import org.chronolens.test.api.repository.AbstractRepositoryTest
import org.chronolens.test.api.repository.RevisionChangeSet
import org.chronolens.test.api.repository.SourceFileChange.ChangeFile
import org.chronolens.test.api.repository.SourceFileChange.DeleteFile
import org.chronolens.test.api.repository.SourceFileChange.InvalidateFile
import org.chronolens.test.api.versioning.vcsProxy

class InteractiveRepositoryTest : AbstractRepositoryTest() {
  override fun createRepository(vararg history: RevisionChangeSet): Repository {
    val parser = FakeParser()
    val revisions =
      history.map { changeSet ->
        changeSet.associate { change ->
          val source =
            when (change) {
              is ChangeFile -> parser.unparse(change.sourceFile)
              is DeleteFile -> null
              is InvalidateFile -> "SyntaxError"
            }
          change.sourcePath.toString() to source
        }
      }
    val vcs = vcsProxy { revisions.forEach(this::revision) }
    return InteractiveRepository(vcs, parser)
  }
}
