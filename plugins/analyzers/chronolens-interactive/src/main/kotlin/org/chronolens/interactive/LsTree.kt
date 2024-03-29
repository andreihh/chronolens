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
import org.chronolens.api.analysis.option
import org.chronolens.api.repository.Repository
import org.chronolens.api.repository.Repository.AccessMode
import org.chronolens.api.repository.Repository.AccessMode.RANDOM_ACCESS
import org.chronolens.model.RevisionId
import org.chronolens.model.SourcePath

public class LsTreeSpec : AnalyzerSpec {
  override val name: String
    get() = "ls-tree"

  override val description: String
    get() = "Prints all the interpretable files of the repository from the specified revision."

  override fun create(optionsProvider: OptionsProvider): LsTreeAnalyzer =
    LsTreeAnalyzer(optionsProvider)
}

public class LsTreeAnalyzer(optionsProvider: OptionsProvider) : Analyzer(optionsProvider) {
  override val accessMode: AccessMode
    get() = RANDOM_ACCESS

  private val rev by
    option<String>()
      .name("rev")
      .alias("r")
      .description("the inspected revision (default: the <head> revision")
      .nullable()
      .transformIfNotNull(::RevisionId)

  override fun analyze(repository: Repository): LsTreeReport {
    val revisionId = rev ?: repository.getHeadId()
    return LsTreeReport(repository.listSources(revisionId))
  }
}

public data class LsTreeReport(val sources: Set<SourcePath>) : Report {
  override val name: String
    get() = "ls-tree"

  override fun toString(): String = sources.joinToString("\n") + "\n"
}
