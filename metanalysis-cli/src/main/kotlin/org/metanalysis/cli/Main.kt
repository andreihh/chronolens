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

fun main(args: Array<String>) {
    if (args.size != 3) {
        println("Usage: vcs file output")
        return
    }
    val vcs = args[0]
    val path = args[1]
    val outputPath = args[2]
    val project = Project(vcs)
    val history = project.getFileHistory(path)
    JsonDriver.serialize(FileOutputStream(outputPath), history)
}
