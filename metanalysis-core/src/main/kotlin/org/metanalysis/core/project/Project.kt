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

import org.metanalysis.core.delta.SourceFileTransaction
import org.metanalysis.core.delta.SourceFileTransaction.Companion.diff
import org.metanalysis.core.model.Parser
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.versioning.VersionControlSystem
import org.metanalysis.core.versioning.VersionControlSystem.Companion.get
import org.metanalysis.core.versioning.VersionControlSystem.Companion.getByName

import java.io.File
import java.io.IOException
import java.util.Date

/**
 * An object which queries the repository in the current working directory for
 * code metadata.
 *
 * @property vcs the VCS behind the repository
 */
class Project @Throws(IOException::class) private constructor(
        private val vcs: VersionControlSystem
) {
    companion object {
        /**
         * Utility factory method.
         *
         * @param vcs the name of the used VCS, or `null` if the VCS should be
         * detected automatically
         * @return the project instance which can query the detected repository
         * for code metadata
         * @throws IOException if any of the following situations appear:
         * - `vcs` is not `null` and is not supported in the current environment
         * - `vcs` is not `null` and no repository could be detected
         * - `vcs` is `null` and no VCS root could be unambiguously detected
         * - the VCS subprocess is interrupted
         * - any input related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic operator fun invoke(vcs: String? = null): Project {
            val vcsInstance = if (vcs != null) vcs.let(::getByName)
                    ?: throw IOException("'$vcs' not supported!")
            else get() ?: throw IOException("VCS root not found or ambiguous!")
            return Project(vcsInstance)
        }
    }

    data class HistoryEntry(
            val commit: String,
            val author: String,
            val date: Date,
            val transaction: SourceFileTransaction?
    )

    data class History(
            private val entries: List<HistoryEntry>
    ) : List<HistoryEntry> by entries

    init {
        if (!vcs.detectRepository()) {
            throw IOException("No '${vcs.name}' repository detected!")
        }
    }

    private val head by lazy { vcs.getHead().id }

    private fun getParser(path: String): Parser =
            Parser.getByExtension(File(path).extension)
                    ?: throw IOException("No parser can interpret '$path'!")

    /**
     * @throws IOException if any of the following situations appear:
     * - `revision` doesn't exist
     * - `path` never existed in `revision` or any of its ancestors
     * - `path` contained invalid code at any point in time
     * - none of the provided parsers can interpret the file at the given `path`
     * - the VCS subprocess is interrupted or terminates abnormally
     * - any input related errors occur
     */
    @Throws(IOException::class)
    fun getFileHistory(path: String, revision: String = head): History {
        val parser = getParser(path)
        val history = arrayListOf<HistoryEntry>()
        var sourceFile = SourceFile()
        vcs.getFileHistory(revision, path).forEach { (id, author, date) ->
            val source = try {
                vcs.getFile(id, path)
            } catch (e: IOException) {
                null
            }
            val newSourceFile = source?.let(parser::parse) ?: SourceFile()
            val transaction = sourceFile.diff(newSourceFile)
            history += HistoryEntry(id, author, date, transaction)
            sourceFile = newSourceFile
        }
        return History(history)
    }

    /**
     * @param path the relative path of the file which should be interpreted
     * @return the parsed code metadata, or `null` if `path` doesn't exist in
     * `revision`
     * @throws IOException if any of the following situations appear:
     * - `revision` doesn't exist
     * - `path` contains invalid code
     * - none of the provided parsers can interpret the file at the given `path`
     * - the VCS subprocess is interrupted or terminates abnormally
     * - any input related errors occur
     */
    @Throws(IOException::class)
    fun getFileModel(path: String, revision: String = head): SourceFile {
        val parser = getParser(path)
        val source = vcs.getFile(revision, path)
        return parser.parse(source)
    }

    /**
     * @throws IOException if any of the following situations appear:
     * - `srcRevision` doesn't exist
     * - `dstRevision` doesn't exist
     * - `path` doesn't exist in either revisions
     * - `path` contained invalid code in either revisions
     * - none of the provided parsers can interpret the file at the given `path`
     * - the VCS subprocess is interrupted or terminates abnormally
     * - any input related errors occur
     */
    @Throws(IOException::class)
    fun getFileDiff(
            path: String,
            srcRevision: String,
            dstRevision: String = head
    ): SourceFileTransaction? {
        val srcSourceFile = getFileModel(path, srcRevision)
        val dstSourceFile = getFileModel(path, dstRevision)
        return srcSourceFile.diff(dstSourceFile)
    }

    /**
     * @throws IOException if any of the following situations appear:
     * - `revision` doesn't exist
     * - the VCS subprocess is interrupted or terminates abnormally
     * - any input related errors occur
     */
    @Throws(IOException::class)
    fun listFiles(revision: String = head): Set<String> =
            vcs.listFiles(revision)
}
