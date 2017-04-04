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

fun usage(): Nothing {
    println("Usage: [ --vcs=\$vcs ] --out=\$path --file=\$path")
    System.exit(1)
    throw IllegalArgumentException()
}

@Throws(IOException::class)
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
    val project = Project(vcs)
    val history = project.getFileHistory(path)
    JsonDriver.serialize(FileOutputStream(output), history)
}
