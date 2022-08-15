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

import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree
import org.chronolens.core.repository.Repository
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply
import java.io.File

@BuilderMarker
public class RepositoryBuilder {
    private var rootDirectory = File(".")
    private val history = mutableListOf<Revision>()
    private val snapshot = SourceTree.empty()

    public fun rootDirectory(directory: File): RepositoryBuilder {
        rootDirectory = directory
        return this
    }

    public fun revision(revision: Revision): RepositoryBuilder {
        +revision
        return this
    }

    public operator fun Revision.unaryPlus() {
        history += this
    }

    public fun build(): Repository =
        object : Repository {
            init {
                check(history.isNotEmpty())
            }

            override fun getHeadId(): RevisionId = history.last().id

            override fun listSources(): Set<SourcePath> =
                snapshot.sources.map(SourceFile::path).toSet()

            override fun listRevisions(): List<RevisionId> = history.map(Revision::id)

            override fun getSource(path: SourcePath): SourceFile? = snapshot[path]

            override fun getHistory(): Sequence<Revision> = history.asSequence()
        }
}

public fun repository(init: Init<RepositoryBuilder>): Repository =
    RepositoryBuilder().apply(init).build()
