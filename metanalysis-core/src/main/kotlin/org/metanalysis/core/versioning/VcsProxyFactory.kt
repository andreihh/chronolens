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

import java.util.ServiceLoader

/**
 * Creates proxies which delegate their operations to the actual version control
 * system (VCS) implementation through subprocesses.
 *
 * [VcsProxy] instances should be created only through a `VcsProxyFactory`.
 *
 * VCS proxy factories must have a public no-arg constructor and must supply an
 * entry in the
 * `META-INF/services/org.metanalysis.core.versioning.VcsProxyFactory`
 * configuration file.
 */
abstract class VcsProxyFactory {
    companion object {
        private val vcsProxyFactories =
                ServiceLoader.load(VcsProxyFactory::class.java)
                        .filter(VcsProxyFactory::isSupported)

        /**
         * Returns the VCS proxy for the repository detected in the current
         * working directory.
         *
         * @return the requested VCS proxy, or `null` if no supported VCS
         * repository was detected or if multiple repositories were detected
         * @throws IllegalStateException if the detected repository is corrupted
         * or empty (doesn't have a [head][VcsProxy.getHead] revision)
         */
        @JvmStatic
        fun detect(): VcsProxy? =
                vcsProxyFactories.mapNotNull(VcsProxyFactory::createProxy)
                        .singleOrNull()
    }

    /**
     * Returns whether the associated VCS is supported in the current
     * environment.
     */
    protected abstract fun isSupported(): Boolean

    /**
     * Returns a proxy for the repository detected in the current working
     * directory.
     *
     * It is required for the associated VCS to be supported.
     *
     * @return the detected proxy, or `null` if no repository was detected
     * @throws IllegalStateException if the detected repository is corrupted or
     * empty (doesn't have a [head][VcsProxy.getHead] revision)
     */
    protected abstract fun createProxy(): VcsProxy?

    /**
     * Returns a VCS proxy for the repository detected in the current working
     * directory.
     *
     * @return the VCS proxy, or `null` if the associated VCS is not supported
     * in this environment or no repository could be detected
     * @throws IllegalStateException if the detected repository is not in a
     * valid state (it is corrupted or doesn't have a [head][VcsProxy.getHead]
     * revision)
     */
    fun connect(): VcsProxy? = if (isSupported()) createProxy() else null
}
