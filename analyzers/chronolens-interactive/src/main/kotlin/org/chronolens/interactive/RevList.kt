/*
 * Copyright 2022-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.interactive

import org.chronolens.api.analysis.Analyzer
import org.chronolens.api.analysis.AnalyzerSpec
import org.chronolens.api.analysis.OptionsProvider
import org.chronolens.api.analysis.Report
import org.chronolens.api.repository.Repository
import org.chronolens.api.repository.Repository.AccessMode
import org.chronolens.api.repository.Repository.AccessMode.RANDOM_ACCESS
import org.chronolens.model.RevisionId

public class RevListSpec : AnalyzerSpec {
  override val name: String
    get() = "rev-list"

  override val description: String
    get() =
      """Prints all revisions on the path from the currently checked-out (<head>) revision to
            the root of the revision tree / graph in chronological order."""

  override fun create(optionsProvider: OptionsProvider): RevListAnalyzer =
    RevListAnalyzer(optionsProvider)
}

public class RevListAnalyzer(optionsProvider: OptionsProvider) : Analyzer(optionsProvider) {
  override val accessMode: AccessMode
    get() = RANDOM_ACCESS

  override fun analyze(repository: Repository): RevListReport =
    RevListReport(repository.listRevisions())
}

public data class RevListReport(val revisions: List<RevisionId>) : Report {
  override fun toString(): String = revisions.joinToString("\n") + "\n"
}
