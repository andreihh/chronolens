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

package org.chronolens.api.database

import java.io.IOException
import org.chronolens.api.analysis.Report
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId

public interface RepositoryDatabase : AutoCloseable {
  @Throws(IOException::class) public fun readHistoryIds(): List<RevisionId>

  @Throws(IOException::class) public fun readHistory(): Sequence<Revision>

  @Throws(IOException::class) public fun writeHistory(revisions: Sequence<Revision>)

  @Throws(IOException::class) public fun appendHistory(revisions: Sequence<Revision>)

  @Throws(IOException::class) public fun readReport(name: String): Report?

  @Throws(IOException::class) public fun writeReport(report: Report)

  @Throws(IOException::class) override fun close()
}
