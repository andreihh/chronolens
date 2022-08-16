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

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import org.chronolens.core.analysis.AnalyzerSpec
import org.chronolens.core.analysis.ErrorReport
import org.chronolens.core.analysis.InvalidOptionException
import org.chronolens.core.analysis.Option
import org.chronolens.core.analysis.option
import org.chronolens.core.repository.CorruptedRepositoryException
import org.chronolens.core.repository.RepositoryConnector
import org.chronolens.core.repository.RepositoryConnector.AccessMode.RANDOM_ACCESS
import org.chronolens.core.repository.persist
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException

/**
 * A command line subcommand that runs an [org.chronolens.core.analysis.Analyzer].
 *
 * @param analyzerSpec the [AnalyzerSpec] used to create the analyzer
 */
class AnalyzerSubcommand(analyzerSpec: AnalyzerSpec, repositoryRootOption: Option<File>) :
    Subcommand(analyzerSpec.name, analyzerSpec.description) {

    private val analyzer = analyzerSpec.create(CommandLineOptionsProvider(this))
    private val repositoryRoot by repositoryRootOption

    override fun execute() {
        try {
            val repository =
                RepositoryConnector.newConnector(repositoryRoot).connect(analyzer.accessMode)
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
        } catch (e: UncheckedIOException) {
            System.err.println("An I/O error occurred!")
            e.printStackTrace()
        }
    }
}

class PersistSubcommand(repositoryRootOption: Option<File>) : Subcommand("persist", "") {
    private val repositoryRoot by repositoryRootOption
    private val connector by lazy { RepositoryConnector.newConnector(repositoryRoot) }

    override fun execute() {
        try {
            connector.connect(RANDOM_ACCESS).persist(connector.openOrCreate())
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}

class CleanSubcommand(repositoryRootOption: Option<File>) : Subcommand("clean", "") {
    private val repositoryRoot by repositoryRootOption

    override fun execute() {
        try {
            RepositoryConnector.newConnector(repositoryRoot).delete()
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}

/** Returns the list of [Subcommand]s for all registered [AnalyzerSpec]s. */
fun ArgParser.assembleAnalyzerSubcommands(): List<Subcommand> {
    val optionsProvider = CommandLineOptionsProvider(this)
    val repositoryRootOption =
        optionsProvider
            .option<String>()
            .name("repository-root")
            .description("")
            .default(".")
            .transform(::File)

    val subcommands = mutableListOf<Subcommand>()
    for (analyzerSpec in AnalyzerSpec.loadAnalyzerSpecs()) {
        subcommands += AnalyzerSubcommand(analyzerSpec, repositoryRootOption)
    }
    subcommands += PersistSubcommand(repositoryRootOption)
    subcommands += CleanSubcommand(repositoryRootOption)
    return subcommands
}
