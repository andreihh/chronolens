/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import org.chronolens.core.cli.MainCommand
import org.chronolens.core.cli.run

class Main : MainCommand() {
    override val name: String get() = "chronolens"
    override val version: String get() = "0.2"
    override val help: String
        get() = """
        ChronoLens is a software evolution analysis tool that inspects the
        repository detected in the current working directory.
    """

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            val command = Main()
            command.registerSubcommands(
                listOf(LsTree(), RevList(), Model(), Persist(), Clean())
            )
            run(command, *args)
        }
    }
}

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    // TODO: make the default main function once tested.
    val parser = ArgParser("chronolens")
    val optionsProvider = CommandLineOptionsProvider(parser)
    parser.strictSubcommandOptionsOrder = true
    parser.subcommands(*optionsProvider.assembleAnalyzerSubcommands().toTypedArray())
    parser.parse(args)
}
