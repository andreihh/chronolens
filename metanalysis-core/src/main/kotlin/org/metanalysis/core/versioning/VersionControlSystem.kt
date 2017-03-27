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

    abstract fun getCommit(id: String): Commit

    abstract fun getCommitHistory(): List<Commit>

    abstract fun getFile(file: String, commitId: String): InputStream

    abstract fun getFileHistory(file: String): List<String>
}
