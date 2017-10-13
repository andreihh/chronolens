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

package org.metanalysis

import org.metanalysis.core.logging.LoggerFactory

import kotlin.system.exitProcess

fun usage(message: String): Nothing {
    throw IllegalUsageException(message)
}

fun printlnErr(message: String?) {
    System.err.println(message)
}

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("org/metanalysis")
    try {
        val command = args.firstOrNull()?.let { Command(it) }
                ?: usage(Command.Help.help)
        command.execute(*args.drop(1).toTypedArray())
    } catch (e: IllegalUsageException) {
        printlnErr(e.message)
        exitProcess(1)
    } catch (e: Exception) {
        logger.severe("${e.message}\n")
        e.printStackTrace(System.err)
        exitProcess(1)
    }
}
