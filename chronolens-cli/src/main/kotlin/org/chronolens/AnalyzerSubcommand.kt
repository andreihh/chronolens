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

import java.io.File
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import org.chronolens.core.analysis.AnalyzerSpec
import org.chronolens.core.analysis.ErrorReport
import org.chronolens.core.analysis.InvalidOptionException
import org.chronolens.core.analysis.Option
import org.chronolens.core.analysis.option
import org.chronolens.core.repository.CorruptedRepositoryException
import org.chronolens.core.repository.InteractiveRepository
import org.chronolens.core.repository.PersistentRepository

/**
 * A command line subcommand that runs an [org.chronolens.core.analysis.Analyzer].
 *
 * @param analyzerSpec the [AnalyzerSpec] used to create the analyzer
 * @param repositoryRootOption the repository root directory option
 */
class AnalyzerSubcommand(analyzerSpec: AnalyzerSpec, repositoryRootOption: Option<File>) :
    Subcommand(analyzerSpec.name, analyzerSpec.description) {

    private val analyzer = analyzerSpec.create(CommandLineOptionsProvider(this))
    private val repositoryRoot by repositoryRootOption

    override fun execute() {
        val repository =
            InteractiveRepository.connect(repositoryRoot)
                ?: PersistentRepository.load(repositoryRoot)
                ?: error("No repository detected in directory '$repositoryRoot'!")
        try {
            val report = analyzer.analyze(repository)
            if (report is ErrorReport) {
                System.err.println(report)
            } else {
                println(report)
            }
        } catch (e: InvalidOptionException) {
            System.err.println("An invalid option has been provided!")
            e.printStackTrace()
        } catch (e: CorruptedRepositoryException) {
            System.err.println("The repository is corrupted!")
            e.printStackTrace()
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
        subcommands += AnalyzerSubcommand(analyzerSpec, repositoryRootOption)
    }
    return subcommands
}
