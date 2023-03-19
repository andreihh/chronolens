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

package org.chronolens.core.repository

import org.chronolens.api.repository.CorruptedRepositoryException
import org.chronolens.model.RevisionId
import org.chronolens.model.SourcePath

/**
 * Throws a [CorruptedRepositoryException] with the given [message].
 *
 * @throws CorruptedRepositoryException
 */
public fun repositoryError(message: String): Nothing {
  throw CorruptedRepositoryException(message)
}

/**
 * Checks that the given [condition] is true.
 *
 * @throws CorruptedRepositoryException if [condition] is false
 */
public fun checkRepositoryState(condition: Boolean, lazyMessage: () -> String) {
  if (!condition) throw CorruptedRepositoryException(lazyMessage())
}

/**
 * Checks that the given revision [id] is valid.
 *
 * @throws CorruptedRepositoryException if the given [id] is invalid
 */
public fun checkValidRevisionId(id: String): RevisionId {
  checkRepositoryState(RevisionId.isValid(id)) { "Invalid revision id '$id'!" }
  return RevisionId(id)
}

/**
 * Checks that the given source file [path] is valid.
 *
 * @throws CorruptedRepositoryException if the given [path] is invalid
 */
public fun checkValidPath(path: String): SourcePath {
  checkRepositoryState(SourcePath.isValid(path)) { "Invalid source file path '$path'!" }
  return SourcePath(path)
}

/**
 * Checks that [this] list of revision ids represent a valid history.
 *
 * @throws CorruptedRepositoryException if [this] list is empty, contains duplicates or invalid
 * revision ids
 */
public fun List<String>.checkValidHistory(): List<RevisionId> {
  checkRepositoryState(isNotEmpty()) { "History must not be empty!" }
  val revisionIds = HashSet<RevisionId>(this.size)
  for (id in this.map(::checkValidRevisionId)) {
    checkRepositoryState(id !in revisionIds) { "Duplicated revision id '$id'!" }
    revisionIds += id
  }
  return this.map(::RevisionId)
}
