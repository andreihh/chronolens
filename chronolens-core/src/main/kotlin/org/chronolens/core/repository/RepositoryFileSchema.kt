/*
 * Copyright 2021-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import org.chronolens.core.model.RevisionId

internal data class RepositoryFileSchema(val repositoryDirectory: File) {
    val rootDirectory: File = File(repositoryDirectory, ".chronolens")
    val historyFile: File = File(rootDirectory, "HISTORY")
    val revisionsDirectory: File = File(rootDirectory, "revisions")

    fun getRevisionsFile(id: RevisionId): File = File(revisionsDirectory, "$id.json")
}
