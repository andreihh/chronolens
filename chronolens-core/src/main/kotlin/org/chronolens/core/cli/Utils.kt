/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

@file:JvmName("Utils")

package org.chronolens.core.cli

import picocli.CommandLine
import picocli.CommandLine.ExecutionException
import picocli.CommandLine.HelpCommand
import picocli.CommandLine.RunLast
import picocli.CommandLine.defaultExceptionHandler
import kotlin.system.exitProcess

/**
 * Prints the given [message] to `stderr` and exits with the specified [status]
 * code.
 *
 * Should be used to validate user input and initial state.
 */
fun exit(message: String, status: Int = 1): Nothing {
    System.err.println(message)
    exitProcess(status)
}

/**
 * Assembles all the provided subcommands, parses the given command-line [args]
 * and runs the [mainCommand], exiting with the specified [status] code if any
 * error occurs.
 *
 * A `help` subcommand is implicitly added.
 */
fun run(mainCommand: Runnable, vararg args: String, status: Int = 1) {
    val cmd = CommandLine(mainCommand).addSubcommand("help", HelpCommand())
    for (subcommand in Subcommand.assembleSubcommands()) {
        cmd.addSubcommand(subcommand.name, subcommand)
    }
    val exceptionHandler = defaultExceptionHandler().andExit(status)
    try {
        cmd.parseWithHandlers(RunLast(), exceptionHandler, *args)
    } catch (e: ExecutionException) {
        System.err.println(e.message)
        e.printStackTrace(System.err)
        exitProcess(status)
    }
}
