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

package org.metanalysis.test.core.versioning

import org.metanalysis.core.versioning.Branch
import org.metanalysis.core.versioning.Commit
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.InputStream

class VersionControlSystemMock(
        private val branches: Set<Branch>,
        private val commits: List<Commit>,
        private val head: Commit,
        private val currentBranch: Branch,
        private val files: Map<Commit, Map<String, InputStream>>,
        private val filesChanged: Map<Commit, Set<String>>
) : VersionControlSystem() {
    companion object {
        val NAME: String = "mockfs"
    }

    init {
        require(currentBranch in branches)
        require(head in commits)
        filesChanged.forEach { commit, changeSet ->
            require(files[commit]?.keys?.containsAll(changeSet) ?: false)
        }
    }

    override val name: String
        get() = NAME

    private val branchesByName = branches.associateBy(Branch::name)
    private val commitsById = commits.associateBy(Commit::id)

    override fun getHead(): Commit = head

    override fun getCommits(branch: Branch): List<Commit> = commits

    override fun getFiles(commit: Commit): Set<String> =
            checkNotNull(files[commit]).keys

    override fun getBranch(name: String): Branch? = branchesByName[name]

    override fun getBranches(): Set<Branch> = branches

    override fun getCommit(id: String): Commit? = commitsById[id]

    override fun getCurrentBranch(): Branch = currentBranch

    override fun getFile(path: String, commit: Commit): InputStream? =
            checkNotNull(files[commit])[path]

    override fun getFileHistory(path: String, branch: Branch): List<Commit> {
        TODO("not implemented")
    }
}
