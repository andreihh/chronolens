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
    val command = args.getOrNull(0) ?: usage()
    val options = args.drop(1).associate {
        if (!it.startsWith("--") || '=' !in it) {
            usage()
        }
        val (option, value) = it.drop(2).split('=', limit = 2)
        option to value
    }
    val vcs = options["vcs"]
    try {
        val project = Project(vcs)
        when (command) {
            "help" -> usage(System.out)
            "get" -> {
                val path = options["file"] ?: usage()
                val sourceFile = project.getFileModel(path)
                if (sourceFile == null) {
                    printlnError("'$path' doesn't exist!")
                    exitProcess(1)
                }
                JsonDriver.serialize(System.out, sourceFile)
            }
            "history" -> {
                val output = options["out"] ?: usage()
                val path = options["file"] ?: usage()
                val history = project.getFileHistory(path)
                JsonDriver.serialize(FileOutputStream(output), history)
            }
            "all" -> {
                val outputDir = options["out"] ?: usage()
                project.listFiles().forEach { path ->
                    try {
                        val history = project.getFileHistory(path)
                        val outputPath = "$outputDir/${path.replace('/', '.')}"
                        JsonDriver.serialize(
                                out = FileOutputStream(outputPath),
                                value = history
                        )
                        printlnError("SUCCESS analyzing '$path'!")
                    } catch (e: IOException) {
                        printlnError("ERROR analyzing '$path': ${e.message}")
                    }
                }
            }
        }
    } catch (e: IOException) {
        printlnError(e.message)
        exitProcess(1)
    }
}
