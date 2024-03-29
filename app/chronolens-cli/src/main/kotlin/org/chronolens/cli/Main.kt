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
@file:JvmName("Main")

package org.chronolens.cli

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import org.chronolens.api.analysis.AnalyzerSpec

fun main(vararg args: String) {
  val parser = ArgParser("chronolens", strictSubcommandOptionsOrder = true)
  parser.subcommands(*assembleSubcommands().toTypedArray())
  parser.parse(args.asList().toTypedArray())
}

private fun assembleSubcommands(): List<Subcommand> =
  AnalyzerSpec.loadAnalyzerSpecs().map(::AnalyzerSubcommand) +
    listOf(PersistSubcommand(), CleanSubcommand())
