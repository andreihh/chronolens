/*
 * Copyright 2022-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.chronolens.api.process.ProcessExecutorProvider
import org.chronolens.api.process.ProcessResult
import org.chronolens.api.versioning.VcsProxy
import org.chronolens.api.versioning.VcsProxyFactory
import org.chronolens.test.api.versioning.AbstractVcsProxyTest
import org.chronolens.test.api.versioning.VcsChangeSet

class GitProxyTest : AbstractVcsProxyTest() {
  private fun executeVcs(directory: File, vararg options: String): ProcessResult =
    ProcessExecutorProvider.INSTANCE.provide(directory).execute("git", *options)

  private fun commit(directory: File, changeSet: VcsChangeSet) {
    for ((path, content) in changeSet) {
      if (content != null) {
        File(directory, path).writeText(content)
      } else {
        assertTrue(File(directory, path).delete())
      }
    }
    executeVcs(directory, "add", "-A")
    executeVcs(directory, "commit", "-m", "Test commit.")
  }

  override fun createRepository(directory: File, vararg revisions: VcsChangeSet): VcsProxy {
    executeVcs(directory, "init")
    executeVcs(directory, "config", "user.email", "t@test.com")
    executeVcs(directory, "config", "user.name", "test")

    for (changeSet in revisions) {
      commit(directory, changeSet)
    }

    return assertNotNull(VcsProxyFactory.connect(directory))
  }
}
