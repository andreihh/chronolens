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

import org.metanalysis.core.delta.SourceFileTransaction.Companion.diff
import org.metanalysis.core.model.Parser
import org.metanalysis.core.model.Parser.SyntaxError
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.serialization.JsonDriver.serialize
import org.metanalysis.core.versioning.VersionControlSystem
import java.io.File

import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 *
 * @property vcs the VCS behind the repository
 */
class InteractiveProject internal constructor(
        private val vcs: VersionControlSystem
) : Project() {
    private val head = vcs.getHead()

    /** The existing files in the `head` revision. */
    private val files: Set<String> = vcs.listFiles()

    private fun getParser(path: String): Parser =
            Parser.getByExtension(path.substringAfterLast('.', ""))
                    ?: throw IOException("No parser can interpret '$path'!")

    @Throws(IOException::class)
    override fun listFiles(): Set<String> = files

    @Throws(IOException::class)
    override fun getFileModel(path: String): SourceFile {
        val parser = getParser(path)
        val source = vcs.getFile(head.id, path)
        return source?.let(parser::parse)
                ?: throw FileNotFoundException("'$path' doesn't exist!")
    }

    @Throws(IOException::class)
    override fun getFileHistory(path: String): List<HistoryEntry> {
        if (path !in files) {
            throw FileNotFoundException("'$path' doesn't exist!")
        }
        val parser = getParser(path)
        val revisions = vcs.getFileHistory(path)
        val history = arrayListOf<HistoryEntry>()
        var sourceFile = SourceFile()
        for ((id, date, author) in revisions) {
            val source = vcs.getFile(id, path)
            val newSourceFile = try {
                source?.let(parser::parse) ?: SourceFile()
            } catch (e: SyntaxError) {
                sourceFile
            }
            val transaction = sourceFile.diff(newSourceFile)
            history += HistoryEntry(id, date, author, transaction)
            sourceFile = newSourceFile
        }
        return history
    }

    @Throws(IOException::class)
    fun persist(): PersistentProject {
        val directory = File(".metanalysis")
        for (path in files) {
            val parent = File(directory, path)
            parent.mkdirs()
            FileOutputStream(File(parent, "model.json")).use { out ->
                serialize(out, getFileModel(path))
            }
            FileOutputStream(File(parent, "history.json")).use { out ->
                serialize(out, getFileHistory(path))
            }
        }
        return PersistentProject(File("."))
    }
}
