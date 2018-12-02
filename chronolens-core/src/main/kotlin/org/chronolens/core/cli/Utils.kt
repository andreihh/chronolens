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
 * Prints the given [message] to `stderr` and exits with status code `1`.
 * code.
 *
 * Should be used to validate user input and initial state.
 */
fun exit(message: String): Nothing {
    System.err.println(message)
    exitProcess(1)
}

/**
 * Assembles all the provided subcommands, parses the given command-line [args]
 * and runs the [mainCommand], exiting with status code `1` if any error occurs.
 *
 * A `help` subcommand is implicitly added.
 */
fun run(mainCommand: MainCommand, vararg args: String) {
    mainCommand.registerSubcommands(Subcommand.assembleSubcommands())
    val cmd = CommandLine(mainCommand.command)
    cmd.addSubcommand("help", HelpCommand())
    val exceptionHandler = defaultExceptionHandler().andExit(1)
    try {
        cmd.parseWithHandlers(RunLast(), exceptionHandler, *args)
    } catch (e: ExecutionException) {
        System.err.println(e.message)
        e.printStackTrace(System.err)
        exitProcess(1)
    }
}

internal fun String.paragraphs(): Array<String> {
    val pars = arrayListOf<String>()
    var par = StringBuilder()
    var newPar = true
    for (line in trimIndent().lines()) {
        if (line.isBlank() && par.isNotBlank()) {
            pars += par.toString()
            par = StringBuilder()
            newPar = true
        } else if (line.isNotBlank()) {
            if (!newPar) {
                par.append(' ')
            }
            par.append(line)
            newPar = false
        }
    }
    if (par.isNotBlank()) {
        pars += par.toString()
    }
    return pars.toTypedArray()
}

internal fun String.words(): List<String> {
    val words = arrayListOf<String>()
    var word = StringBuilder()
    for (char in this.capitalize()) {
        if (char.isUpperCase()) {
            if (!word.isBlank()) {
                words += word.toString()
                word = StringBuilder()
            }
            word.append(char.toLowerCase())
        } else {
            word.append(char)
        }
    }
    if (word.isNotBlank()) {
        words += word.toString()
    }
    return words
}

internal fun getOptionName(propertyName: String): String =
    propertyName.words().joinToString(separator = "-", prefix = "--")
