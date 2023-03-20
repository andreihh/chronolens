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
import org.chronolens.api.analysis.Option.Companion.optionError
import org.chronolens.api.analysis.OptionsProvider
import org.chronolens.api.analysis.Report
import org.chronolens.api.analysis.option
import org.chronolens.api.repository.Repository
import org.chronolens.api.repository.Repository.AccessMode
import org.chronolens.api.repository.Repository.AccessMode.RANDOM_ACCESS
import org.chronolens.model.QualifiedSourceNodeId
import org.chronolens.model.RevisionId
import org.chronolens.model.SourceNode
import org.chronolens.model.walkSourceTree

public class ModelSpec : AnalyzerSpec {
  override val name: String
    get() = "model"

  override val description: String
    get() = "Prints the requested source node from the specified revision."

  override fun create(optionsProvider: OptionsProvider): ModelAnalyzer =
    ModelAnalyzer(optionsProvider)
}

public class ModelAnalyzer(optionsProvider: OptionsProvider) : Analyzer(optionsProvider) {
  override val accessMode: AccessMode
    get() = RANDOM_ACCESS

  private val rev by
    option<String>()
      .name("rev")
      .alias("r")
      .description("the inspected revision (default: the <head> revision")
      .nullable()
      .transformIfNotNull(::RevisionId)

  private val qualifiedId by
    option<String>()
      .name("qualified-id")
      .description("the qualified id of the inspected source node")
      .required()
      .transform { QualifiedSourceNodeId.parseFrom(it) }

  override fun analyze(repository: Repository): ModelReport {
    val revisionId = rev ?: repository.getHeadId()
    val path = qualifiedId.sourcePath
    val model =
      repository.getSource(path, revisionId)
        ?: optionError("File '$path' couldn't be interpreted or doesn't exist!")
    val node =
      model.walkSourceTree().find { it.qualifiedId == qualifiedId }?.sourceNode
        ?: optionError("Source node '$qualifiedId' doesn't exist!")
    return ModelReport(node)
  }
}

public data class ModelReport(val sourceNode: SourceNode) : Report {
  override val name: String
    get() = "model"

  override fun toString(): String = PrettyPrinter.stringify(sourceNode)
}
