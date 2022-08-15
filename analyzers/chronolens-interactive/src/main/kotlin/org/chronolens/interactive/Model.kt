/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.analysis.Analyzer
import org.chronolens.core.analysis.AnalyzerSpec
import org.chronolens.core.analysis.OptionsProvider
import org.chronolens.core.analysis.Report
import org.chronolens.core.analysis.option
import org.chronolens.core.analysis.optionError
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceNode
import org.chronolens.core.model.parseQualifiedSourceNodeIdFrom
import org.chronolens.core.model.walkSourceTree
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.RepositoryConnector.AccessMode
import org.chronolens.core.repository.RepositoryConnector.AccessMode.RANDOM_ACCESS

public class ModelSpec : AnalyzerSpec {
    override val name: String
        get() = "model"

    override val description: String
        get() = ""

    override fun create(optionsProvider: OptionsProvider): ModelAnalyzer =
        ModelAnalyzer(optionsProvider)
}

public class ModelAnalyzer(optionsProvider: OptionsProvider) : Analyzer(optionsProvider) {
    override val accessMode: AccessMode
        get() = RANDOM_ACCESS

    private val rev by option<String>()
        .name("rev")
        .alias("r")
        .description("the inspected revision (default: the <head> revision")
        .nullable()
        .transformIfNotNull(::RevisionId)

    private val qualifiedId by option<String>()
        .name("qualified-id")
        .description("")
        .required()
        .transform(::parseQualifiedSourceNodeIdFrom)

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
    // TODO: pretty-print.
    override fun toString(): String = sourceNode.toString()
}
