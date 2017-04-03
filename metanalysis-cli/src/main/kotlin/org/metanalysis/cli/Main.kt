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

@Throws(IOException::class)
fun main(args: Array<String>) {
    if (args.size !in 1..3) {
        println("Usage: [--vcs=\$vcs] [--output=\$output] \$path")
        return
    }
    val options = args.filter { it.startsWith("--") }.associate {
        val (option, value) = it.split('=')
        option to value
    }
    val vcs = options["--vcs"]
    val path = args.filter { !it.startsWith("--") }.single()
    val output = options["--output"] ?: ".metanalysis/$path-history.json"
    val project = Project(vcs)
    val history = project.getFileHistory(path)
    JsonDriver.serialize(FileOutputStream(output), history)
}
