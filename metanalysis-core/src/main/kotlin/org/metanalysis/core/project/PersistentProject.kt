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

import org.metanalysis.core.logging.LoggerFactory
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.serialization.JsonDriver.deserialize
import org.metanalysis.core.serialization.JsonDriver.serialize

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class PersistentProject private constructor() : Project() {
    companion object {
        private val logger = LoggerFactory.getLogger<PersistentProject>()

        private fun getRootDirectory(): File = File(".metanalysis")

        private fun getObjectDirectory(path: String): File =
                File(File(getRootDirectory(), "objects"), path)

        private fun getFilesFile(): File =
                File(getRootDirectory(), "files.json")

        private fun getModelFile(path: String): File =
                File(getObjectDirectory(path), "model.json")

        private fun getHistoryFile(path: String): File =
                File(getObjectDirectory(path), "history.json")

        @Throws(IOException::class)
        @JvmStatic fun Project.persist(): PersistentProject = when {
            this is PersistentProject -> this
            else -> {
                val files = listFiles()
                serialize(getFilesFile(), files)
                for (path in files) {
                    try {
                        val model = getFileModel(path)
                        serialize(getModelFile(path), model)
                        logger.info("'$path' model OK!")
                    } catch (e: IOException) {
                        logger.warning(e.message)
                    }
                    try {
                        val history = getFileHistory(path)
                        serialize(getHistoryFile(path), history)
                        logger.info("'$path' history OK!")
                    } catch (e: IOException) {
                        logger.warning(e.message)
                    }
                }
                PersistentProject()
            }
        }

        /**
         * Utility factory method.
         *
         * @throws IOException if any input related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic fun load(): PersistentProject? =
                if (getRootDirectory().exists()) PersistentProject()
                else null

        @Throws(IOException::class)
        @JvmStatic fun clean() {
            getRootDirectory().deleteRecursively()
        }
    }

    private val files = deserialize<Array<String>>(getFilesFile()).toSet()

    private fun validateFile(path: String) {
        if (path !in files) {
            throw FileNotFoundException("'$path' doesn't exist in the project!")
        }
    }

    override fun listFiles(): Set<String> = files

    @Throws(IOException::class)
    override fun getFileModel(path: String): SourceFile {
        validateFile(path)
        val modelFile = getModelFile(path)
        if (!modelFile.exists()) {
            throw IOException("'$path' couldn't be interpreted!")
        }
        return deserialize(modelFile)
    }

    @Throws(IOException::class)
    override fun getFileHistory(path: String): List<HistoryEntry> {
        validateFile(path)
        val historyFile = getHistoryFile(path)
        if (!historyFile.exists()) {
            throw IOException("'$path' couldn't be interpreted!")
        }
        return deserialize<Array<HistoryEntry>>(historyFile).asList()
    }
}
