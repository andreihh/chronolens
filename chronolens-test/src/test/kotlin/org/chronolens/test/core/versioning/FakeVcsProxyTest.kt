/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.core.versioning

import java.io.File
import kotlin.test.BeforeTest
import org.chronolens.core.versioning.VcsProxy

class FakeVcsProxyTest : AbstractVcsProxyTest() {
    @BeforeTest
    fun clearRepositories() {
        FakeVcsProxyFactory.deleteAll()
    }

    override fun createRepository(directory: File, vararg revisions: VcsChangeSet): VcsProxy =
        FakeVcsProxyFactory.createRepository(directory) { revisions.forEach(this::revision) }
}
