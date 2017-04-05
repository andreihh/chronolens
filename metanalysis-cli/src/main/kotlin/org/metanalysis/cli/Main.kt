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

import org.metanalysis.core.project.Project
import org.metanalysis.core.serialization.JsonDriver

import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream

import kotlin.system.exitProcess

fun usage(stream: PrintStream = System.err): Nothing {
    stream.println("Usage: [--vcs=\$vcs] --out=\$path --file=\$path")
    exitProcess(1)
}

fun printlnError(line: String?) {
    System.err.println("metanalysis: $line")
}

fun main(args: Array<String>) {
    val options = args.associate {
        if (!it.startsWith("--") || '=' !in it) {
            usage()
        }
        val (option, value) = it.drop(2).split('=', limit = 2)
        option to value
    }
    val vcs = options["vcs"]
    val output = options["out"] ?: usage()
    val path = options["file"] ?: usage()
    try {
        val project = Project(vcs)
        val history = project.getFileHistory(path)
        JsonDriver.serialize(FileOutputStream(output), history)
    } catch (e: IOException) {
        printlnError(e.message)
        exitProcess(1)
    }
}
