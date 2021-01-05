/*
 * Copyright 2017-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.versioning

import org.junit.Test
import java.io.File
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.fail

class VcsProxyFactoryTest {
    class UnsupportedVcsProxyFactory : VcsProxyFactory() {
        override fun isSupported() = false
        override fun createProxy(directory: File) = fail()
    }

    class UndetectedVcsProxyFactory : VcsProxyFactory() {
        override fun isSupported() = true
        override fun createProxy(directory: File) = null
    }

    @Test fun `test detect ignores unsupported`() {
        VcsProxyFactoryMock.resetRepository()
        val vcs = VcsProxyFactory.detect(File("."))
        assertNull(vcs)
    }

    @Test fun `test detect ignores undetected`() {
        VcsProxyFactoryMock.resetRepository()
        val vcs = VcsProxyFactory.detect(File("."))
        assertNull(vcs)
    }

    @Test fun `test detect uninitialized repository returns null`() {
        VcsProxyFactoryMock.resetRepository()
        val vcs = VcsProxyFactory.detect(File("."))
        assertNull(vcs)
    }

    @Test fun `test detect initialized repository returns non-null`() {
        VcsProxyFactoryMock.setRepository(emptyList())
        val vcs = VcsProxyFactory.detect(File("."))
        assertNotNull(vcs)
    }

    @Test fun `test connect to unsupported returns null`() {
        val vcsFactory = UnsupportedVcsProxyFactory()
        val vcs = vcsFactory.connect(File("."))
        assertNull(vcs)
    }

    @Test fun `test connect to undetected returns null`() {
        val vcsFactory = UndetectedVcsProxyFactory()
        val vcs = vcsFactory.connect(File("."))
        assertNull(vcs)
    }

    @Test fun `test connect to uninitialized repository returns non-null`() {
        VcsProxyFactoryMock.resetRepository()
        val vcsFactory = VcsProxyFactoryMock()
        val vcs = vcsFactory.connect(File("."))
        assertNull(vcs)
    }

    @Test fun `test connect to initialized repository returns non-null`() {
        VcsProxyFactoryMock.setRepository(emptyList())
        val vcsFactory = VcsProxyFactoryMock()
        val vcs = vcsFactory.connect(File("."))
        assertNotNull(vcs)
    }

    @Test fun `test get non-existing revision throws`() {
        VcsProxyFactoryMock.setRepository(emptyList())
        val vcs = checkNotNull(VcsProxyFactory.detect(File(".")))
        assertFailsWith<IllegalArgumentException> {
            vcs.getRevision("non-existing")
        }
    }
}
