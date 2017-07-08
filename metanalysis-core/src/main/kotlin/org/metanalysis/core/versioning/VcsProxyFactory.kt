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

import java.io.IOException
import java.util.ServiceLoader

/**
 * Creates proxies which delegate their operations to the actual version control
 * system (VCS) implementation through subprocesses.
 *
 * [VcsProxy] instances should be created only through a `VcsProxyFactory`.
 *
 * VCS proxy factories must have a public no-arg constructor.
 *
 * The file
 * `META-INF/services/org.metanalysis.core.versioning.VcsProxyFactory` must
 * be provided and must contain the list of all implemented VCS proxy factories.
 */
abstract class VcsProxyFactory {
    companion object {
        private val vcsProxyFactories = ServiceLoader
                .load(VcsProxyFactory::class.java)
                .filter(VcsProxyFactory::isSupported)

        /**
         * Returns the VCS for the repository detected in the current working
         * directory.
         *
         * @return the requested VCS, or `null` if no supported VCS repository
         * was detected or if multiple repositories were detected
         * @throws IllegalStateException if the detected repository is corrupted
         * or empty (doesn't have a [head][VcsProxy.getHead] revision)
         * @throws IOException if any input related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic fun detect(): VcsProxy? =
                vcsProxyFactories
                        .filter(VcsProxyFactory::isRepository)
                        .singleOrNull()
                        ?.createProxy()
    }

    /**
     * Returns whether the associated VCS is supported in the current
     * environment.
     *
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    protected abstract fun isSupported(): Boolean

    /**
     * Returns whether a repository was detected in the current working
     * directory.
     *
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    protected abstract fun isRepository(): Boolean

    /**
     * Returns a proxy for the repository detected in the current working
     * directory.
     *
     * It is required for the associated VCS to be supported and a repository to
     * exist in the current working directory.
     *
     * @throws IllegalStateException if the detected repository is corrupted or
     * empty (doesn't have a [head][VcsProxy.getHead] revision)
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    protected abstract fun createProxy(): VcsProxy

    /**
     * Returns a VCS proxy for the repository detected in the current working
     * directory.
     *
     * @return the VCS proxy, or `null` if the associated VCS is not supported
     * in this environment or no repository could be detected
     * @throws IllegalStateException if the detected repository is not in a
     * valid state (it is corrupted or doesn't have a [head][VcsProxy.getHead]
     * revision)
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    fun connect(): VcsProxy? =
            if (isSupported() && isRepository()) createProxy()
            else null
}
