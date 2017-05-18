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
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.FileNotFoundException
import java.io.IOException
import java.util.Date

/**
 * An object which queries the project located in the current working directory
 * for code metadata.
 */
abstract class Project {
    /**
     * @property revision the unique identifier of the revision
     * @property date the date at which the change was committed
     * @property author the author of the revision
     * @property transaction the changes applied in the revision
     */
    data class HistoryEntry(
            val revision: String,
            val date: Date,
            val author: String,
            val transaction: SourceFileTransaction?
    )

    /**
     * Returns all the existing files in the `head` revision.
     *
     * @return the set of existing files in the `head` revision
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun listFiles(): Set<String>

    /**
     * Returns the code metadata of the file at the given `path` as it is found
     * in the `head` revision.
     *
     * @param path the relative path of the file which should be interpreted
     * @return the parsed code metadata, or `null` if the given `path` doesn't
     * exist
     * @throws FileNotFoundException if the given `path` doesn't exist
     * @throws IOException if the file at the given `path` couldn't be
     * interpreted or if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getFileModel(path: String): SourceFile

    /**
     * If the file contains invalid code in a previous revision, the changes
     * applied in that revision are replaced with a `null` transaction.
     *
     * @param path the path of the file which should be analyzed
     * @return
     * @throws FileNotFoundException if `path` doesn't exist
     * @throws IOException if the file at the given `path` couldn't be
     * interpreted or if any input related errors occur
     */
    @Throws(IOException::class)
    abstract fun getFileHistory(path: String): List<HistoryEntry>
}
