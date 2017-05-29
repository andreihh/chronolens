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

import org.junit.Test
import org.metanalysis.test.core.versioning.VcsProxyFactoryMock

import org.metanalysis.test.core.versioning.VcsProxyMock

import kotlin.test.assertNull
import kotlin.test.assertTrue

class VcsProxyFactoryTest {
    class UnsupportedVcsProxyFactory : VcsProxyFactory() {
        override fun isSupported(): Boolean = false
        override fun isRepository(): Boolean = false
        override fun createProxy(): VcsProxy = throw AssertionError()
    }

    class UndetectedVcsProxyFactory : VcsProxyFactory() {
        override fun isSupported(): Boolean = true
        override fun isRepository(): Boolean = false
        override fun createProxy(): VcsProxy = throw AssertionError()
    }

    @Test fun `test detect vcs ignores unsupported`() {
        VcsProxyFactoryMock.resetRepository()
        val vcs = VcsProxyFactory.detect()
        assertNull(vcs)
    }

    @Test fun `test detect vcs ignores undetected`() {
        VcsProxyFactoryMock.resetRepository()
        val vcs = VcsProxyFactory.detect()
        assertNull(vcs)
    }

    @Test fun `test detect initialized vcs returns non-null`() {
        VcsProxyFactoryMock.setRepository(emptyList())
        val vcs = VcsProxyFactory.detect()
        assertTrue(vcs is VcsProxyMock)
    }
}
