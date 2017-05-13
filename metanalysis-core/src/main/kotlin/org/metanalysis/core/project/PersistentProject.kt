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

package org.metanalysis.core.project

import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.serialization.JsonDriver.deserialize
import org.metanalysis.core.serialization.JsonDriver.serialize

import java.io.File
import java.io.IOException

class PersistentProject internal constructor(
        private val directory: File
) : Project() {
    companion object {
        @Throws(IOException::class)
        @JvmStatic fun Project.persist(): PersistentProject = when {
            this is PersistentProject -> this
            else -> {
                val directory = File(".metanalysis")
                directory.mkdirs()
                val files = hashSetOf<String>()
                for (path in listFiles()) {
                    try {
                        val model = getFileModel(path)
                        val history = getFileHistory(path)
                        val parent = File(File(directory, "objects"), path)
                        parent.mkdirs()
                        File(parent, "model.json").outputStream().use { out ->
                            serialize(out, model)
                        }
                        File(parent, "history.json").outputStream().use { out ->
                            serialize(out, history)
                        }
                        files += path
                    } catch (e: IOException) {
                        System.err.println(e.message)
                    }
                }
                File(directory, "files.json").outputStream().use { out ->
                    serialize(out, files)
                }
                PersistentProject(File("."))
            }
        }
    }

    private val files by lazy {
        File(directory, ".metanalysis/files.json").inputStream().use { src ->
            deserialize<Array<String>>(src).toSet()
        }
    }

    override fun listFiles(): Set<String> = files

    override fun getFileModel(path: String): SourceFile {
        val parent = File(File(directory, ".metanalysis/objects"), path)
        val model = File(parent, "model.json")
        return model.inputStream().use { src ->
            deserialize<SourceFile>(src)
        }
    }

    override fun getFileHistory(path: String): List<HistoryEntry> {
        val parent = File(File(directory, ".metanalysis/objects"), path)
        val history = File(parent, "history.json")
        return history.inputStream().use { src ->
            deserialize<Array<HistoryEntry>>(src).asList()
        }
    }
}
