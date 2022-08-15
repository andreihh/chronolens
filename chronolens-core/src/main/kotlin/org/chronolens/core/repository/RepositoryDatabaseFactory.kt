/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.repository

import org.chronolens.core.repository.Repository.HistoryProgressListener
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException

public object RepositoryDatabaseFactory {
    /** The directory within the repository root where all database files should be stored. */
    public const val DATABASE_ROOT_DIRECTORY: String = ".chronolens"

    /**
     * Creates a new [RepositoryDatabase] in [rootDirectory], persists [this] repository to it and
     * reports the progress to the given [listener].
     *
     * @throws CorruptedRepositoryException if the repository is corrupted
     * @throws IOException if any I/O errors occur
     */
    @Throws(IOException::class)
    public fun Repository.persist(
        rootDirectory: File,
        listener: HistoryProgressListener? = null
    ): RepositoryDatabase {
        mkdirs(File(rootDirectory, DATABASE_ROOT_DIRECTORY))
        val database = RepositoryFileDatabase(rootDirectory)
        database.writeHistory(getHistory(listener))
        return database
    }

    @Throws(IOException::class)
    public fun detect(rootDirectory: File): RepositoryDatabase? =
        if (!File(rootDirectory, DATABASE_ROOT_DIRECTORY).isDirectory) null
        else RepositoryFileDatabase(rootDirectory)

    /**
     * Deletes the repository database from the given [rootDirectory].
     *
     * All corresponding [RepositoryDatabase] and [PersistentRepository] instances will become
     * corrupted after this method is called.
     *
     * @throws UncheckedIOException if any I/O errors occur
     */
    @Throws(IOException::class)
    public fun clean(rootDirectory: File) {
        File(rootDirectory, DATABASE_ROOT_DIRECTORY).deleteRecursively()
    }
}
