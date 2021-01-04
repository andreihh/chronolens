/*
 * Copyright 2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import java.io.File

internal data class RepositoryFileSchema(
    private val repositoryDirectory: File
) {

    val rootDirectory: File = File(repositoryDirectory, ".chronolens")
    val headFile: File = File(rootDirectory, "HEAD")
    val sourcesFile: File = File(rootDirectory, "SOURCES")
    val historyFile: File = File(rootDirectory, "HISTORY")
    val snapshotDirectory: File = File(rootDirectory, "snapshot")
    val transactionsDirectory: File = File(rootDirectory, "transactions")

    fun getSourceDirectory(path: String): File = File(snapshotDirectory, path)

    fun getSourceFile(path: String): File =
        File(getSourceDirectory(path), "model.json")

    fun getTransactionFile(id: String): File =
        File(transactionsDirectory, "$id.json")
}
