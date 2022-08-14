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

@file:OptIn(ExperimentalCli::class)

package org.chronolens

import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import org.chronolens.core.analysis.AnalyzerSpec
import org.chronolens.core.analysis.ErrorReport
import org.chronolens.core.analysis.Option
import org.chronolens.core.repository.InteractiveRepository
import org.chronolens.core.repository.PersistentRepository
import java.io.File

/**
 * A command line subcommand that runs an [org.chronolens.core.analysis.Analyzer].
 *
 * @param analyzerSpec the [AnalyzerSpec] used to create the analyzer
 * @param repositoryRootOption the repository root directory option
 */
class AnalyzerSubcommand(
    analyzerSpec: AnalyzerSpec,
    repositoryRootOption: Option<File>
) : Subcommand(analyzerSpec.name, analyzerSpec.description) {

    private val analyzer = analyzerSpec.create(CommandLineOptionsProvider(this))
    private val repositoryRoot by repositoryRootOption

    override fun execute() {
        val repository =
            InteractiveRepository.connect(repositoryRoot)
                ?: PersistentRepository.load(repositoryRoot)
                ?: error("No repository detected in directory '$repositoryRoot'!")
        val report = analyzer.analyze(repository)
        if (report is ErrorReport) {
            println(report)
        } else {
            // TODO: serialize the report.
            println(report)
        }
    }
}

/**
 * Defines common [org.chronolens.core.analysis.Analyzer] options and returns the list of
 * [Subcommand]s for all registered [AnalyzerSpec]s.
 */
fun CommandLineOptionsProvider.assembleAnalyzerSubcommands(): List<Subcommand> {
    val repositoryRootOption =
        option<String>().name("repository-root").default(".").transform(::File)
    val subcommands = mutableListOf<Subcommand>()
    for (analyzerSpec in AnalyzerSpec.loadAnalyzerSpecs()) {
        subcommands += analyzerSpec.toSubcommand(repositoryRootOption)
    }
    return subcommands
}

private fun AnalyzerSpec.toSubcommand(repositoryRootOption: Option<File>): Subcommand =
    AnalyzerSubcommand(this, repositoryRootOption)
