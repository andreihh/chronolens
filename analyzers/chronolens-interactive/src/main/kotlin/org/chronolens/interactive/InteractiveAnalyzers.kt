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
import org.chronolens.core.analysis.Analyzer.Mode.FAST_HISTORY
import org.chronolens.core.analysis.Analyzer.Mode.RANDOM_ACCESS
import org.chronolens.core.analysis.AnalyzerSpec
import org.chronolens.core.analysis.OptionsProvider
import org.chronolens.core.analysis.Report
import org.chronolens.core.analysis.option
import org.chronolens.core.analysis.optionError
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceNode
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.parseQualifiedSourceNodeIdFrom
import org.chronolens.core.model.walkSourceTree
import org.chronolens.core.repository.Repository

public class LsTree : AnalyzerSpec {
    override val name: String
        get() = "ls-tree"

    override val description: String
        get() = "Prints all the interpretable files of the repository from the specified revision."

    override fun create(optionsProvider: OptionsProvider): Analyzer =
        object : Analyzer(optionsProvider) {
            override val mode: Mode
                get() = RANDOM_ACCESS

            private val rev by option<String>()
                .name("rev")
                .alias("r")
                .description("the inspected revision (default: the <head> revision")
                .nullable()
                .transformIfNotNull(::RevisionId)

            override fun analyze(repository: Repository): Report {
                val revisionId = rev ?: repository.getHeadId()
                return LsTreeReport(repository.listSources(revisionId))
            }
        }

    public data class LsTreeReport(val sources: Set<SourcePath>) : Report {
        override fun toString(): String = sources.joinToString("\n")
    }
}

public class RevList : AnalyzerSpec {
    override val name: String
        get() = "rev-list"

    override val description: String
        get() = """
        Prints all revisions on the path from the currently checked-out (<head>) revision to the
        root of the revision tree / graph in chronological order.
        """.trimIndent()

    override fun create(optionsProvider: OptionsProvider): Analyzer =
        object : Analyzer(optionsProvider) {
            override val mode: Mode
                get() = RANDOM_ACCESS

            override fun analyze(repository: Repository): RevListReport =
                RevListReport(repository.listRevisions())
        }

    public data class RevListReport(val revisions: List<RevisionId>) : Report {
        override fun toString(): String = revisions.joinToString("\n")
    }
}

public class Model : AnalyzerSpec {
    override val name: String
        get() = "model"

    override val description: String
        get() = ""

    override fun create(optionsProvider: OptionsProvider): Analyzer =
        object : Analyzer(optionsProvider) {
            override val mode: Mode
                get() = RANDOM_ACCESS

            private val rev by option<String>()
                .name("rev")
                .alias("r")
                .description("the inspected revision (default: the <head> revision")
                .nullable()
                .transformIfNotNull(::RevisionId)

            private val qualifiedId by option<String>().name("qualified-id").description("")
                .required().transform(::parseQualifiedSourceNodeIdFrom)

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
}

public class Persist : AnalyzerSpec {
    override val name: String
        get() = "persist"

    override val description: String
        get() = ""

    override fun create(optionsProvider: OptionsProvider): Analyzer =
        object : Analyzer(optionsProvider) {
            override val mode: Mode
                get() = RANDOM_ACCESS

            override fun analyze(repository: Repository): Report = TODO("not implemented")
        }
}

public class Clean : AnalyzerSpec {
    override val name: String
        get() = "clean"

    override val description: String
        get() = "Deletes the previously persisted repository from the current working directory, if it exists."

    override fun create(optionsProvider: OptionsProvider): Analyzer =
        object : Analyzer(optionsProvider) {
            override val mode: Mode
                get() = FAST_HISTORY

            override fun analyze(repository: Repository): Report = TODO("not implemented")
        }

    public object CleanReport : Report {
        override fun toString(): String = "Repository clean OK!"
    }
}
