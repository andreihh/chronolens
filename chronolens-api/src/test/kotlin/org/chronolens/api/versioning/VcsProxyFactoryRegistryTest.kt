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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.chronolens.test.api.versioning.FakeVcsProxyFactory
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class VcsProxyFactoryRegistryTest {
  class UndetectedVcsProxyFactory : VcsProxyFactory {
    override fun clone(url: URL, directory: File): VcsProxy? = null
    override fun connect(directory: File): VcsProxy? = null
  }

  @get:Rule val tmp: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

  private val directory by lazy { tmp.root }

  @BeforeTest
  fun clearRepositories() {
    FakeVcsProxyFactory.deleteAll()
  }

  @Test
  fun clone_whenUndetected_returnsNonNull() {
    assertNotNull(VcsProxyFactory.clone(URL("https:/example.com/repository.git"), File("./")))
  }

  @Test
  fun clone_whenInitialized_returnsNonNull() {
    val url = URL("https:/example.com/repository.git")
    FakeVcsProxyFactory.createRemoteRepository(url) {}

    assertNotNull(VcsProxyFactory.clone(url, File("./")))
  }

  @Test
  fun connect_whenUndetected_returnsNull() {
    assertNull(VcsProxyFactory.connect(directory))
  }

  @Test
  fun connect_whenInitialized_returnsNonNull() {
    FakeVcsProxyFactory.createRepository(directory) {}

    assertNotNull(VcsProxyFactory.connect(directory))
  }
}
