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

@file:JvmName("Main")

package org.metanalysis.cli

import java.io.IOException

import kotlin.system.exitProcess

fun printlnError(line: String?) {
    System.err.println("metanalysis: $line")
}

fun exitError(message: String?): Nothing {
    printlnError(message)
    exitProcess(1)
}

fun main(args: Array<String>) {
    try {
        val command = args.firstOrNull()
                ?.let { Command(it) }
                ?: Command.Help
        command.execute(*args.drop(1).toTypedArray())
    } catch (e: IOException) {
        exitError(e.message)
    } catch (e: IllegalUsageException) {
        exitError(e.message)
    }
}
