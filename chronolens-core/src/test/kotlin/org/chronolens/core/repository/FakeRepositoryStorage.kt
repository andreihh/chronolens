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

import java.io.IOException
import java.io.UncheckedIOException
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId

// TODO: move back to chronolens-test.
class FakeRepositoryStorage : RepositoryStorage {
  private var history = emptyList<Revision>()
  private var error: IOException? = null

  @Throws(IOException::class)
  override fun readHistoryIds(): List<String> {
    throwIfError()
    return history.map(Revision::id).map(RevisionId::toString)
  }

  @Throws(IOException::class)
  override fun readHistory(): Sequence<Revision> {
    throwIfError()
    return history.asSequence().map { revision ->
      throwUncheckedIfError()
      revision
    }
  }

  @Throws(IOException::class)
  override fun writeHistory(revisions: Sequence<Revision>) {
    throwIfError()
    history = revisions.toList()
  }

  public fun setError(error: IOException) {
    this.error = error
  }

  public fun clearError() {
    this.error = null
  }

  @Throws(IOException::class)
  private fun throwIfError() {
    error?.let { throw it }
  }

  private fun throwUncheckedIfError() {
    error?.let { throw UncheckedIOException(it) }
  }
}
