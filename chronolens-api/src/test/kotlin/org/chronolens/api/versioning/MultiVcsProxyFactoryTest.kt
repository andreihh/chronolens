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
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MultiVcsProxyFactoryTest {
  @Test
  fun clone_whenHasProvidedVcs_returnsNonNull() {
    val url = URL("file:///~/repository.git")
    val directory = File("./")
    val vcs2 = mock<VcsProxy>()
    val vcsFactory1 = mock<VcsProxyFactory> { on { clone(any(), any()) } doReturn null }
    val vcsFactory2 = mock<VcsProxyFactory> { on { clone(url, directory) } doReturn vcs2 }

    val multiVcsFactory = MultiVcsProxyFactory(listOf(vcsFactory1, vcsFactory2))

    assertSame(expected = vcs2, actual = multiVcsFactory.clone(url, directory))
  }

  @Test
  fun clone_whenNoProvidedVcs_returnsNull() {
    val vcsFactory1 = mock<VcsProxyFactory> { on { clone(any(), any()) } doReturn null }
    val vcsFactory2 = mock<VcsProxyFactory> { on { clone(any(), any()) } doReturn null }

    val multiVcsFactory = MultiVcsProxyFactory(listOf(vcsFactory1, vcsFactory2))

    assertNull(multiVcsFactory.clone(URL("file:///~/repository.git"), File("./")))
  }

  @Test
  fun connect_whenHasProvidedVcs_returnsNonNull() {
    val directory = File("./")
    val vcs2 = mock<VcsProxy>()
    val vcsFactory1 = mock<VcsProxyFactory> { on { connect(any()) } doReturn null }
    val vcsFactory2 = mock<VcsProxyFactory> { on { connect(directory) } doReturn vcs2 }

    val multiVcsFactory = MultiVcsProxyFactory(listOf(vcsFactory1, vcsFactory2))

    assertSame(expected = vcs2, actual = multiVcsFactory.connect(directory))
  }

  @Test
  fun connect_whenNoProvidedVcs_returnsNull() {
    val vcsFactory1 = mock<VcsProxyFactory> { on { connect(any()) } doReturn null }
    val vcsFactory2 = mock<VcsProxyFactory> { on { connect(any()) } doReturn null }

    val multiVcsFactory = MultiVcsProxyFactory(listOf(vcsFactory1, vcsFactory2))

    assertNull(multiVcsFactory.connect(File("./")))
  }
}
