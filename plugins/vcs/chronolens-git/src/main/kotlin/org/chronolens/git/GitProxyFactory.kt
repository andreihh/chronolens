/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import java.net.URL
import org.chronolens.api.process.ProcessExecutorProvider
import org.chronolens.api.versioning.VcsProxy
import org.chronolens.api.versioning.VcsProxyFactory

/** Creates proxies that delegate their operations to the `git` VCS. */
internal class GitProxyFactory : VcsProxyFactory {
  private val vcs: String = "git"

  private fun getPrefix(directory: File): String? {
    val result =
      ProcessExecutorProvider.INSTANCE.provide(directory).execute(vcs, "rev-parse", "--show-prefix")
    val rawPrefix = result.getOrNull() ?: return null
    return rawPrefix.lines().first()
  }

  override fun clone(url: URL, directory: File): VcsProxy? {
    val cloneResult =
      ProcessExecutorProvider.INSTANCE.provide(directory)
        .execute(vcs, "clone", url.toString(), "./")
    return if (cloneResult.isSuccess) connect(directory) else null
  }

  override fun connect(directory: File): VcsProxy? {
    val prefix = getPrefix(directory) ?: return null
    return if (prefix.isBlank()) GitProxy(directory) else null
  }
}
