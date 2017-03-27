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

package org.metanalysis.core.versioning

import java.io.InputStream
import java.util.ServiceLoader

/**
 * An abstract version control system.
 *
 * Version control systems must have a public no-arg constructor.
 *
 * The file
 * `META-INF/services/org.metanalysis.core.versioning.VersionControlSystem` must
 * be provided and must contain the list of all provided version control
 * systems.
 */
abstract class VersionControlSystem {
    companion object {
        private val vcss = ServiceLoader.load(VersionControlSystem::class.java)
        private val nameToVcs = vcss.associateBy(VersionControlSystem::name)

        /**
         * Returns the version control system with the given `name`.
         *
         * @param name the name of the requested version control system
         * @return the requested version control system, or `null` if no such
         * system was provided
         */
        @JvmStatic fun getByName(name: String): VersionControlSystem? =
                nameToVcs[name]
    }

    /** The name of this version control system. */
    abstract val name: String

    abstract fun getBranches(): Set<Branch>

    abstract fun getBranch(name: String): Branch?

    /**
     * @throws IllegalStateException if the `head` is detached
     */
    abstract fun getCurrentBranch(): Branch

    abstract fun getCommits(branch: Branch): List<Commit>

    abstract fun getCommit(id: String): Commit?

    /**
     * @throws IllegalStateException if the repository doesn't have any commits
     */
    abstract fun getHead(): Commit

    abstract fun getFiles(commit: Commit): Set<String>

    abstract fun getFile(path: String, commit: Commit): InputStream?

    abstract fun getFileHistory(path: String, branch: Branch): List<Commit>
}
