/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

@file:JvmName("Builders")

package org.chronolens.test.core.repository

import org.chronolens.core.model.AddNode
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTreeEdit
import org.chronolens.core.model.qualifiedSourcePathOf
import org.chronolens.core.repository.Repository
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply
import org.chronolens.test.core.repository.SourceFileChange.ChangeFile
import org.chronolens.test.core.repository.SourceFileChange.DeleteFile
import org.chronolens.test.core.repository.SourceFileChange.InvalidateFile
import java.time.Instant

@BuilderMarker
public class RepositoryBuilder {
    private val history = mutableListOf<Revision>()

    public fun revision(revision: Revision): RepositoryBuilder {
        history += revision
        return this
    }

    public operator fun Revision.unaryPlus() {
        revision(this)
    }

    public fun build(): Repository = FakeRepository(history)
}

public fun repository(init: Init<RepositoryBuilder>): Repository =
    RepositoryBuilder().apply(init).build()

public fun repository(vararg history: RevisionChangeSet): Repository {
    // TODO: receive a Parser as a parameter.
    val repositoryBuilder = RepositoryBuilder()
    val sources = mutableMapOf<SourcePath, SourceFile>()

    fun getRemoveEdits(path: SourcePath): List<SourceTreeEdit> =
        if (path in sources) listOf(RemoveNode(qualifiedSourcePathOf(path)))
        else emptyList()

    fun getAddEdits(sourceFile: SourceFile): List<SourceTreeEdit> =
        getRemoveEdits(sourceFile.path) +
            listOf(AddNode(qualifiedSourcePathOf(sourceFile.path), sourceFile))

    for ((index, changeSet) in history.withIndex()) {
        val revisionId = RevisionId(index.toString())
        val edits = mutableListOf<SourceTreeEdit>()

        for (change in changeSet) {
            when (change) {
                is ChangeFile -> {
                    require(change.sourcePath.toString().endsWith(".fake")) {
                        "Cannot parse source path '${change.sourcePath}'!"
                    }
                    edits += getAddEdits(change.sourceFile)
                    sources[change.sourcePath] = change.sourceFile
                }
                is DeleteFile -> {
                    edits += getRemoveEdits(change.sourcePath)
                    sources -= change.sourcePath
                }
                is InvalidateFile -> {
                    val canParse = change.sourcePath.toString().endsWith(".fake")
                    if (canParse && change.sourcePath !in sources) {
                        val emptySourceFile = SourceFile(change.sourcePath)
                        edits += getAddEdits(emptySourceFile)
                        sources[change.sourcePath] = emptySourceFile
                    }
                }
            }
        }

        val revision = Revision(revisionId, date = Instant.now(), author = "t@test.com", edits)
        repositoryBuilder.revision(revision)
    }

    return repositoryBuilder.build()
}
