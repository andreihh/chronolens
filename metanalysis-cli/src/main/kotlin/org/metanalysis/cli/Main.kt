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

fun exitError(message: String?): Nothing {
    printlnError(message)
    exitProcess(1)
}

fun getCommand(project: Project, options: Map<String, String>) {
    val path = options["file"] ?: usage()
    val sourceFile = project.getFileModel(path)
            ?: exitError("'$path' doesn't exist!")
    val out = options["out"]?.let(::FileOutputStream)
    if (out == null) {
        JsonDriver.serialize(System.out, sourceFile)
    } else {
        out.use { JsonDriver.serialize(it, sourceFile) }
    }
}

private fun Project.analyzeHistory(path: String, outDir: String) {
    try {
        val history = getFileHistory(path)
        val outPath = path.replace('/', '.')
        FileOutputStream(File(outDir, outPath)).use { out ->
            JsonDriver.serialize(out, history)
            printlnError("SUCCESS analyzing '$path'!")
        }
    } catch (e: IOException) {
        printlnError("ERROR at '$path': ${e.message}")
    }
}

fun historyCommand(project: Project, options: Map<String, String>) {
    val outDir = options["out-dir"] ?: usage()
    if ("all" in options) {
        project.listFiles().forEach { path ->
            project.analyzeHistory(path, outDir)
        }
    } else {
        val path = options["file"] ?: usage()
        project.analyzeHistory(path, outDir)
    }
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
    val project = Project.create()
            ?: exitError("No supported VCS repository detected!")
    try {
        when (command) {
            "help" -> usage(System.out)
            "get" -> getCommand(project, options)
            "history" -> historyCommand(project, options)
            "list" -> project.listFiles().forEach(::println)
            else -> usage()
        }
    } catch (e: Exception) {
        exitError(e.message)
    }
}
