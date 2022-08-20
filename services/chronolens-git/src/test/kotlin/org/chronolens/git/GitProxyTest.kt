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

package org.chronolens.git

import org.chronolens.core.subprocess.Subprocess
import org.chronolens.core.versioning.VcsProxy
import org.chronolens.core.versioning.VcsProxyFactory
import org.chronolens.test.core.versioning.AbstractVcsProxyTest
import org.chronolens.test.core.versioning.VcsChangeSet
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GitProxyTest : AbstractVcsProxyTest() {
    private fun commit(directory: File, changeSet: VcsChangeSet) {
        for ((path, content) in changeSet) {
            if (content != null) {
                File(directory, path).writeText(content)
            } else {
                assertTrue(File(directory, path).delete())
            }
        }
        Subprocess.execute(directory, "git", "add", "-A")
        Subprocess.execute(directory, "git", "commit", "-m", "Test commit.")
    }

    override fun createRepository(directory: File, vararg revisions: VcsChangeSet): VcsProxy {
        Subprocess.execute(directory, "git", "init")
        Subprocess.execute(directory, "git", "config", "user.email", "t@test.com")
        Subprocess.execute(directory, "git", "config", "user.name", "test")

        for (changeSet in revisions) {
            commit(directory, changeSet)
        }

        return assertNotNull(VcsProxyFactory.detect(directory))
    }
}
