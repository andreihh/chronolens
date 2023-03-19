/*
 * Copyright 2017-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import java.util.ServiceLoader

/**
 * Creates proxies which delegate their operations to the actual version control system (VCS)
 * implementation through subprocesses.
 *
 * [VcsProxy] instances should be created only through a [VcsProxyFactory].
 *
 * VCS proxy factories must have a public no-arg constructor and must supply an entry in the
 * `META-INF/services/org.chronolens.core.versioning.VcsProxyFactory` configuration file.
 */
public interface VcsProxyFactory {
  /**
   * Clones the repository located at the given [url] into the given root [directory] and returns a
   * VCS proxy connected to it, or `null` if the associated VCS is not supported in this
   * environment, the [url] is not a valid repository, or the repository couldn't be cloned (e.g.,
   * due to network errors).
   *
   * @throws IllegalStateException if the cloned repository is not in a valid state (it is corrupted
   * or doesn't have a [head][VcsProxy.getHead] revision)
   */
  public fun clone(url: URL, directory: File): VcsProxy?

  /**
   * Returns a VCS proxy for the repository detected in the given root [directory], or `null` if the
   * associated VCS is not supported in this environment or no repository could be detected.
   *
   * @throws IllegalStateException if the detected repository is not in a valid state (it is
   * corrupted or doesn't have a [head][VcsProxy.getHead] revision)
   */
  public fun connect(directory: File): VcsProxy?

  /** A [MultiVcsProxyFactory] encompassing all registered [VcsProxyFactories][VcsProxyFactory]. */
  public companion object Registry :
    VcsProxyFactory by MultiVcsProxyFactory(ServiceLoader.load(VcsProxyFactory::class.java))
}
