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

package org.chronolens.test.core.repository

import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply
import org.chronolens.test.core.repository.SourceFileChange.ChangeFile
import org.chronolens.test.core.repository.SourceFileChange.DeleteFile
import org.chronolens.test.core.repository.SourceFileChange.InvalidateFile

@BuilderMarker
public class RevisionChangeSetBuilder {
    private val changeSet = mutableSetOf<SourceFileChange>()

    public fun change(sourceFile: SourceFile): RevisionChangeSetBuilder {
        changeSet += ChangeFile(sourceFile)
        return this
    }

    public operator fun SourceFile.unaryPlus() {
        change(this)
    }

    public fun invalidate(sourcePath: SourcePath): RevisionChangeSetBuilder {
        changeSet += InvalidateFile(sourcePath)
        return this
    }

    public fun invalidate(sourcePath: String): RevisionChangeSetBuilder =
        invalidate(SourcePath(sourcePath))

    public fun delete(sourcePath: SourcePath): RevisionChangeSetBuilder {
        changeSet += DeleteFile(sourcePath)
        return this
    }

    public fun delete(sourcePath: String): RevisionChangeSetBuilder = delete(SourcePath(sourcePath))

    public fun build(): RevisionChangeSet = changeSet
}

public fun revisionChangeSet(init: Init<RevisionChangeSetBuilder>): RevisionChangeSet =
    RevisionChangeSetBuilder().apply(init).build()
