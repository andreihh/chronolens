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

package org.chronolens.test.api.database

import java.io.IOException
import java.io.UncheckedIOException
import org.chronolens.api.analysis.Report
import org.chronolens.api.database.RepositoryDatabase
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId

// TODO: add FakeDatabase and FakeDatabaseProvider.
public class FakeRepositoryDatabase : RepositoryDatabase {
  private var history = emptyList<Revision>()
  private val reports = mutableMapOf<String, Report>()
  private var closed: Boolean = false
  private var error: IOException? = null

  @Throws(IOException::class)
  override fun readHistoryIds(): List<RevisionId> {
    throwIfClosed()
    throwIfError()
    return history.map(Revision::id)
  }

  @Throws(IOException::class)
  override fun readHistory(): Sequence<Revision> {
    throwIfClosed()
    throwIfError()
    return history.asSequence().map { revision ->
      throwUncheckedIfClosed()
      throwUncheckedIfError()
      revision
    }
  }

  @Throws(IOException::class)
  override fun writeHistory(revisions: Sequence<Revision>) {
    throwIfClosed()
    throwIfError()
    history = revisions.toList()
  }

  @Throws(IOException::class)
  override fun appendHistory(revisions: Sequence<Revision>) {
    throwIfClosed()
    throwIfError()
    history = history + revisions.toList()
  }

  @Throws(IOException::class)
  override fun readReport(name: String): Report? {
    throwIfClosed()
    throwIfError()
    return reports[name]
  }

  @Throws(IOException::class)
  override fun writeReport(report: Report) {
    throwIfClosed()
    throwIfError()
    reports[report.name] = report
  }

  @Throws(IOException::class)
  override fun close() {
    throwIfError()
    closed = true
  }

  public fun setError(error: IOException) {
    this.error = error
  }

  public fun clearError() {
    this.error = null
  }

  @Throws(IOException::class)
  private fun throwIfClosed() {
    if (closed) {
      throw IOException("Database was already closed!")
    }
  }

  private fun throwUncheckedIfClosed() {
    if (closed) {
      throw UncheckedIOException(IOException("Database was already closed!"))
    }
  }

  @Throws(IOException::class)
  private fun throwIfError() {
    error?.let { throw it }
  }

  private fun throwUncheckedIfError() {
    error?.let { throw UncheckedIOException(it) }
  }
}
