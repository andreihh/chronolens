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
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.parsing.Parser
import org.metanalysis.core.parsing.SyntaxErrorException
import org.metanalysis.core.versioning.VcsProxy
import org.metanalysis.core.versioning.VcsProxyFactory

import java.io.FileNotFoundException
import java.io.IOException

/**
 *
 * @property vcs the VCS behind the repository
 */
class InteractiveProject private constructor(
        private val vcs: VcsProxy
) : Project() {
    companion object {
        /**
         * Utility factory method.
         *
         * @return the project instance which can query the detected repository
         * for code metadata, or `null` if no supported VCS repository could be
         * unambiguously detected
         * @throws IllegalStateException if the `head` revision doesn't exist
         * @throws IOException if any input related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic fun connect(): InteractiveProject? =
                VcsProxyFactory.detect()?.let(::InteractiveProject)
    }

    private val head = vcs.getHead()
    private val files = vcs.listFiles()

    private fun validatePath(path: String) {
        if (path !in files) {
            throw FileNotFoundException("'$path' doesn't exist!")
        }
    }

    private fun getParser(path: String): Parser =
            Parser.getByExtension(path.substringAfterLast('.', ""))
                    ?: throw IOException("No parser can interpret '$path'!")

    override fun listFiles(): Set<String> = files

    @Throws(IOException::class)
    override fun getFileModel(path: String): SourceFile {
        validatePath(path)
        val parser = getParser(path)
        val source = vcs.getFile(head.id, path)!!
        return parser.parse(source)
    }

    @Throws(IOException::class)
    override fun getFileHistory(path: String): List<HistoryEntry> {
        validatePath(path)
        val parser = getParser(path)
        val revisions = vcs.getFileHistory(path)
        val history = arrayListOf<HistoryEntry>()
        var sourceFile = SourceFile()
        for ((id, date, author) in revisions) {
            val source = vcs.getFile(id, path)
            val newSourceFile = try {
                source?.let(parser::parse) ?: SourceFile()
            } catch (e: SyntaxErrorException) {
                sourceFile
            }
            val transaction = sourceFile.diff(newSourceFile)
            history += HistoryEntry(id, date, author, transaction)
            sourceFile = newSourceFile
        }
        return history
    }
}
