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

import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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

    @Test fun `test detect ignores unsupported`() {
        VcsProxyFactoryMock.resetRepository()
        val vcs = VcsProxyFactory.detect()
        assertNull(vcs)
    }

    @Test fun `test detect ignores undetected`() {
        VcsProxyFactoryMock.resetRepository()
        val vcs = VcsProxyFactory.detect()
        assertNull(vcs)
    }

    @Test fun `test detect uninitialized repository returns null`() {
        VcsProxyFactoryMock.resetRepository()
        val vcs = VcsProxyFactory.detect()
        assertNull(vcs)
    }

    @Test fun `test detect initialized repository returns non-null`() {
        VcsProxyFactoryMock.setRepository(emptyList())
        val vcs = VcsProxyFactory.detect()
        assertNotNull(vcs)
    }

    @Test fun `test connect to unsupported returns null`() {
        val vcsFactory = UnsupportedVcsProxyFactory()
        val vcs = vcsFactory.connect()
        assertNull(vcs)
    }

    @Test fun `test connect to undetected returns null`() {
        val vcsFactory = UndetectedVcsProxyFactory()
        val vcs = vcsFactory.connect()
        assertNull(vcs)
    }

    @Test fun `test connect to uninitialized repository returns non-null`() {
        VcsProxyFactoryMock.resetRepository()
        val vcsFactory = VcsProxyFactoryMock()
        val vcs = vcsFactory.connect()
        assertNull(vcs)
    }

    @Test fun `test connect to initialized repository returns non-null`() {
        VcsProxyFactoryMock.setRepository(emptyList())
        val vcsFactory = VcsProxyFactoryMock()
        val vcs = vcsFactory.connect()
        assertNotNull(vcs)
    }

    @Test fun `test get head from empty repository throws`() {
        VcsProxyFactoryMock.setRepository(emptyList())
        val vcs = checkNotNull(VcsProxyFactory.detect())
        assertFailsWith<RevisionNotFoundException> {
            vcs.getHead()
        }
    }
}
