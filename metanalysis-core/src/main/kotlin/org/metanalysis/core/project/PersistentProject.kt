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

import java.io.File

class PersistentProject internal constructor(
        private val directory: File
) : Project() {
    private val files by lazy {
        File(directory, "files.json").inputStream().use { src ->
            deserialize<Array<String>>(src).toSet()
        }
    }

    override fun listFiles(): Set<String> = files

    override fun getFileModel(path: String): SourceFile {
        val parent = File(directory, path)
        val model = File(parent, "model.json")
        return model.inputStream().use { src ->
            deserialize<SourceFile>(src)
        }
    }

    override fun getFileHistory(path: String): List<HistoryEntry> {
        val parent = File(directory, path)
        val history = File(parent, "history.json")
        return history.inputStream().use { src ->
            deserialize<Array<HistoryEntry>>(src).asList()
        }
    }
}
