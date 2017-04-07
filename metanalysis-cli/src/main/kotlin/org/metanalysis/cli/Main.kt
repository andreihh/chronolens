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

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream

import kotlin.system.exitProcess

fun usage(stream: PrintStream = System.err): Nothing {
    stream.println(
    """Usage: (get [--out=<path>] --file=<path>)
       | (history --out-dir=<path> [--file=<path> | --all])
       | list
       | help"""
    )
    exitProcess(1)
}

fun printlnError(line: String?) {
    System.err.println("metanalysis: $line")
}

fun main(args: Array<String>) {
    val command = args.getOrNull(0) ?: usage()
    val options = args.drop(1).associate {
        if (!it.startsWith("--")) {
            usage()
        }
        val option = it.removePrefix("--").substringBefore('=')
        val value = it.substringAfter('=', "")
        option to value
    }
    try {
        val project = Project()
        when (command) {
            "help" -> usage(System.out)
            "get" -> {
                val path = options["file"] ?: usage()
                val out = options["out"]?.let(::FileOutputStream) ?: System.out
                val sourceFile = project.getFileModel(path)
                JsonDriver.serialize(out, sourceFile)
            }
            "history" -> {
                val outDir = options["out-dir"] ?: usage()
                if ("all" in options) {
                    project.listFiles().forEach { path ->
                        try {
                            val outPath = path.replace('/', '.')
                            val out = FileOutputStream(File(outDir, outPath))
                            val history = project.getFileHistory(path)
                            JsonDriver.serialize(out, history)
                            printlnError("SUCCESS analyzing '$path'!")
                        } catch (e: IOException) {
                            printlnError("ERROR at '$path': ${e.message}")
                        }
                    }
                } else {
                    val path = options["file"] ?: usage()
                    val outPath = path.replace('/', '.')
                    val out = FileOutputStream(File(outDir, outPath))
                    val history = project.getFileHistory(path)
                    JsonDriver.serialize(out, history)
                }
            }
            "list" -> project.listFiles().forEach(::println)
            else -> usage()
        }
    } catch (e: IOException) {
        printlnError(e.message)
        exitProcess(1)
    }
}
