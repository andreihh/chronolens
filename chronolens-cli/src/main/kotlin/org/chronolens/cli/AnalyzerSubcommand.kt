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

package org.chronolens.cli

import java.io.UncheckedIOException
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import org.chronolens.core.analysis.AnalysisException
import org.chronolens.core.analysis.AnalyzerSpec
import org.chronolens.core.analysis.InvalidOptionException
import org.chronolens.core.repository.CorruptedRepositoryException
import org.chronolens.core.repository.RepositoryConnector

/**
 * A command line subcommand that runs an [org.chronolens.core.analysis.Analyzer].
 *
 * @param analyzerSpec the [AnalyzerSpec] used to create the analyzer
 */
class AnalyzerSubcommand(analyzerSpec: AnalyzerSpec) :
    Subcommand(analyzerSpec.name, analyzerSpec.description) {

    private val optionsProvider = CommandLineOptionsProvider(this)
    private val repositoryRoot by optionsProvider.repositoryRootOption()
    private val analyzer = analyzerSpec.create(optionsProvider)

    override fun execute() {
        try {
            val repository =
                RepositoryConnector.newConnector(repositoryRoot).connect(analyzer.accessMode)
            val report = analyzer.analyze(repository)
            println(report)
        } catch (e: InvalidOptionException) {
            System.err.println("An invalid option has been provided!")
            e.printStackTrace()
        } catch (e: CorruptedRepositoryException) {
            System.err.println("The repository is corrupted!")
            e.printStackTrace()
        } catch (e: UncheckedIOException) {
            System.err.println("An I/O error occurred!")
            e.printStackTrace()
        } catch (e: AnalysisException) {
            System.err.println("An error occurred while analyzing the repository!")
            e.printStackTrace()
        }
    }
}
