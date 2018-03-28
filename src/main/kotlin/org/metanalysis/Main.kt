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

import picocli.CommandLine
import picocli.CommandLine.ExecutionException
import picocli.CommandLine.RunAll
import picocli.CommandLine.defaultExceptionHandler
import kotlin.system.exitProcess

fun exit(message: String): Nothing {
    System.err.println(message)
    exitProcess(1)
}

fun main(vararg args: String) {
    val cmd = CommandLine(MainCommand())
    val exceptionHandler = defaultExceptionHandler().andExit(1)
    try {
        cmd.parseWithHandlers(RunAll(), exceptionHandler, *args)
    } catch (e: ExecutionException) {
        System.err.println(e.message)
        e.printStackTrace(System.err)
        exitProcess(1)
    }
}
