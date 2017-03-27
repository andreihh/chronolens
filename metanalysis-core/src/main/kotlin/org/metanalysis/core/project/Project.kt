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

import org.metanalysis.core.Parser
import org.metanalysis.core.SourceFile
import org.metanalysis.core.delta.SourceFileTransaction
import org.metanalysis.core.delta.SourceFileTransaction.Companion.diff
import org.metanalysis.core.versioning.Commit
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.File
import java.io.IOException

data class Project(
        val vcs: VersionControlSystem,
        val repository: String,
        val head: String
) {
    companion object {
        @JvmStatic fun init(vcs: VersionControlSystem): Project {
            TODO()
        }
    }

    fun update() {
        TODO()
    }

    fun getFileHistory(file: String): List<SourceFile> {
        TODO()
    }

    fun getDiffHistory(file: String): List<SourceFileTransaction> {
        TODO()
    }

    fun getCommitHistory(file: String): List<Commit> {
        TODO()
    }

    @Throws(IOException::class)
    fun getFile(file: String, commitId: String = head): SourceFile {
        Parser.getByExtension(File(file).extension)
                ?.parse(vcs.getFile(file, commitId))
        TODO()
    }

    @Throws(IOException::class)
    fun getFileDiff(
            file: String,
            srcCommitId: String,
            dstCommitId: String = head
    ): SourceFileTransaction? =
            getFile(file, srcCommitId).diff(getFile(file, dstCommitId))
}
