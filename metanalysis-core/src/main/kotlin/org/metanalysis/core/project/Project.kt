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
import org.metanalysis.core.model.Parser.SyntaxError
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.versioning.VersionControlSystem
import org.metanalysis.core.versioning.VersionControlSystem.Companion.get

import java.io.IOException
import java.util.Date

/**
 * An object which queries the repository in the current working directory for
 * code metadata.
 *
 * @property vcs the VCS behind the repository
 */
class Project private constructor(private val vcs: VersionControlSystem) {
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
        @JvmStatic fun create(): Project? = get()?.let(::Project)
    }

    data class HistoryEntry(
            val revision: String,
            val date: Date,
            val author: String,
            val transaction: SourceFileTransaction?
    )

    data class History(
            private val entries: List<HistoryEntry>
    ) : List<HistoryEntry> by entries

    private val head = vcs.getHead().id

    private fun getParser(path: String): Parser =
            Parser.getByExtension(path.substringAfterLast('.', ""))
                    ?: throw IOException("No parser can interpret '$path'!")

    /**
     * Returns the code metadata of the file at the given `path` as it is found
     * in `revision`.
     *
     * @param path the relative path of the file which should be interpreted
     * @param revision the desired revision of the file
     * @return the parsed code metadata, or `null` if the given `path` doesn't
     * exist in `revision`
     * @throws IOException if any of the following situations appear:
     * - `revision` doesn't exist
     * - none of the provided parsers can interpret the file at the given `path`
     * - the file at the given `path` contains invalid code
     * - any input related errors occur
     */
    @Throws(IOException::class)
    fun getFileModel(path: String, revision: String = head): SourceFile? {
        val parser = getParser(path)
        val source = vcs.getFile(vcs.getRevision(revision), path)
        return source?.let(parser::parse)
    }

    /**
     * Returns the differences between the given revisions of the file at the
     * given `path`.
     *
     * @param path the relative path of the file which should be inspected
     * @param srcRevision the source revision
     * @param dstRevision the destination revision
     * @return the transaction which should be applied on the `path` contents as
     * they appear in `srcRevision` in order to obtain the contents as they
     * appear in `dstRevision`, or `null` if `path` is unchanged between the two
     * revisions
     * @throws IOException if any of the following situations appear:
     * - `srcRevision` doesn't exist
     * - `dstRevision` doesn't exist
     * - `path` contained invalid code in either revisions
     * - none of the provided parsers can interpret the file at the given `path`
     * - any input related errors occur
     */
    @Throws(IOException::class)
    fun getFileDiff(
            path: String,
            srcRevision: String,
            dstRevision: String = head
    ): SourceFileTransaction? {
        val srcSourceFile = getFileModel(path, srcRevision) ?: SourceFile()
        val dstSourceFile = getFileModel(path, dstRevision) ?: SourceFile()
        return srcSourceFile.diff(dstSourceFile)
    }

    /**
     * If the file contains invalid code in any revision, the changes applied in
     * that revision are replaced with a `null` transaction.
     *
     * @param path the relative path of the file which should be analyzed
     * @param revision the last revision in the history of the file which should
     * be analyzed
     * @return
     * @throws IOException if any of the following situations appear:
     * - `revision` doesn't exist
     * - `path` never existed in `revision` or any of its ancestors
     * - none of the provided parsers can interpret the file at the given `path`
     * - any input related errors occur
     */
    @Throws(IOException::class)
    fun getFileHistory(path: String, revision: String = head): History {
        val parser = getParser(path)
        val revisions = vcs.getFileHistory(vcs.getRevision(revision), path)
        val history = arrayListOf<HistoryEntry>()
        var sourceFile = SourceFile()
        for (rev in revisions) {
            val source = vcs.getFile(rev, path)
            val newSourceFile = try {
                source?.let(parser::parse) ?: SourceFile()
            } catch (e: SyntaxError) {
                sourceFile
            }
            val transaction = sourceFile.diff(newSourceFile)
            history += HistoryEntry(rev.id, rev.date, rev.author, transaction)
            sourceFile = newSourceFile
        }
        return History(history)
    }

    /**
     * Returns all the existing files in `revision`.
     *
     * @param revision the inspected revision
     * @return the set of existing files in `revision`
     * @throws IOException if `revision` doesn't exist or any input related
     * errors occur
     */
    @Throws(IOException::class)
    fun listFiles(revision: String = head): Set<String> =
            vcs.listFiles(vcs.getRevision(revision))
}
