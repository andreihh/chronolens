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

import org.metanalysis.core.delta.SourceFileTransaction.Companion.diff
import org.metanalysis.core.serialization.JsonDriver
import org.metanalysis.java.JavaParser

import java.io.File
import java.io.FileOutputStream

fun main(args: Array<String>) {
    if (args.size != 3) {
        println("Usage: file_v1 file_v2 output_file")
        return
    }
    val parser = JavaParser()
    val file1 = File(args[0])
    val file2 = File(args[1])
    val sourceFile1 = parser.parse(file1)
    val sourceFile2 = parser.parse(file2)
    val diff = sourceFile1.diff(sourceFile2)
    val file3 = File(args[2])
    if (diff == null) {
        println("The two files have the same code metadata!")
        return
    }
    JsonDriver.serialize(FileOutputStream(file3), diff)
}
