/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.api.versioning

import java.io.File
import org.chronolens.api.versioning.VcsProxy
import org.chronolens.api.versioning.VcsProxyFactory
import org.chronolens.test.Init

public class FakeVcsProxyFactory : VcsProxyFactory() {
  override fun isSupported(): Boolean = true

  override fun createProxy(directory: File): VcsProxy? = repositories[directory]

  public companion object {
    private val repositories = mutableMapOf<File, VcsProxy>()

    @JvmStatic
    public fun createRepository(directory: File, init: Init<VcsProxyBuilder>): VcsProxy {
      repositories[directory] = vcsProxy(init)
      return repositories.getValue(directory)
    }

    @JvmStatic
    public fun deleteRepository(directory: File) {
      repositories -= directory
    }

    @JvmStatic
    public fun deleteAll() {
      repositories.clear()
    }
  }
}
