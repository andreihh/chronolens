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

@file:OptIn(ExperimentalCli::class)

package org.chronolens.cli

import java.io.IOException
import java.io.UncheckedIOException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberFunctions
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import org.chronolens.api.analysis.AnalyzerSpec
import org.chronolens.api.analysis.InvalidOptionException
import org.chronolens.api.serialization.SerializationException
import org.chronolens.core.repository.RepositoryConnector
import org.chronolens.core.serialization.JsonModule

/**
 * A command line subcommand that runs an [org.chronolens.api.analysis.Analyzer].
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
      val repository = RepositoryConnector.newConnector(repositoryRoot).connect(analyzer.accessMode)
      val report = analyzer.analyze(repository)
      if (hasToString(report::class)) {
        print(report)
      } else {
        println(JsonModule.stringify(report))
      }
    } catch (e: InvalidOptionException) {
      System.err.println("An invalid option has been provided!")
      e.printStackTrace()
    } catch (e: SerializationException) {
      System.err.println("A serialization error occurred!")
      e.printStackTrace()
    } catch (e: UncheckedIOException) {
      System.err.println("An I/O error occurred!")
      e.printStackTrace()
    } catch (e: IOException) {
      System.err.println("An I/O error occurred!")
      e.printStackTrace()
    }
  }
}

private fun hasToString(type: KClass<*>): Boolean = type.declaredMemberFunctions.any(::isToString)

private fun isToString(function: KFunction<*>): Boolean =
  with(function) {
    name == "toString" && parameters.size == 1 && returnType == String::class.createType()
  }
