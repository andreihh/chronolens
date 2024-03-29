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

package org.chronolens.api.versioning

import java.io.File
import java.net.URL

/**
 * A [VcsProxyFactory] capable of connecting to multiple version control systems by delegating to
 * one of the provided [vcsProxyFactories].
 */
internal class MultiVcsProxyFactory(private val vcsProxyFactories: Iterable<VcsProxyFactory>) :
  VcsProxyFactory {

  override fun clone(url: URL, directory: File): VcsProxy? =
    vcsProxyFactories.firstNotNullOfOrNull { it.clone(url, directory) }

  override fun connect(directory: File): VcsProxy? =
    vcsProxyFactories.firstNotNullOfOrNull { it.connect(directory) }
}
