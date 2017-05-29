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
 * Creates proxies which delegate their operations to the actual VCS
 * implementation through subprocesses.
 *
 * `VcsProxy` instances should be create only through a `VcsProxyFactory`.
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
     * Returns whether this VCS is supported in this environment.
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
     */
    protected abstract fun createProxy(): VcsProxy

    /**
     * Returns a VCS proxy for the repository detected in the current working
     * directory, or `null` if this VCS is not supported or no repository could
     * be unambiguously detected.
     */
    fun connect(): VcsProxy? =
            if (isSupported() && isRepository()) createProxy()
            else null
}
