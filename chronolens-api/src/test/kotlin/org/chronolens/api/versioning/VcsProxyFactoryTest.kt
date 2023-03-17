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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.fail
import org.chronolens.test.api.versioning.FakeVcsProxyFactory
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class VcsProxyFactoryTest {
  class UnsupportedVcsProxyFactory : VcsProxyFactory() {
    override fun isSupported() = false
    override fun createProxy(directory: File) = fail()
  }

  class UndetectedVcsProxyFactory : VcsProxyFactory() {
    override fun isSupported() = true
    override fun createProxy(directory: File) = null
  }

  @get:Rule val tmp: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

  private val directory by lazy { tmp.root }

  @BeforeTest
  fun clearRepositories() {
    FakeVcsProxyFactory.deleteAll()
  }

  @Test
  fun detect_whenUnsupportedUndetectedUninitialized_returnsNull() {
    assertNull(VcsProxyFactory.detect(directory))
  }

  @Test
  fun detect_whenInitialized_returnsNonNull() {
    FakeVcsProxyFactory.createRepository(directory) {}

    assertNotNull(VcsProxyFactory.detect(directory))
  }

  @Test
  fun connect_whenUnsupported_returnsNull() {
    assertNull(UnsupportedVcsProxyFactory().connect(directory))
  }

  @Test
  fun connect_whenUndetected_returnsNull() {
    assertNull(UndetectedVcsProxyFactory().connect(directory))
  }

  @Test
  fun connect_whenUninitialized_returnsNull() {
    assertNull(FakeVcsProxyFactory().connect(directory))
  }

  @Test
  fun connect_whenInitialized_returnsNonNull() {
    FakeVcsProxyFactory.createRepository(directory) {}

    assertNotNull(FakeVcsProxyFactory().connect(directory))
  }
}
