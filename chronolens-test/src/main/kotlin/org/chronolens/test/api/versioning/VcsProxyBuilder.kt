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

package org.chronolens.test.api.versioning

import org.chronolens.api.versioning.VcsProxy
import org.chronolens.test.BuilderMarker
import org.chronolens.test.Init
import org.chronolens.test.apply

@BuilderMarker
public class VcsProxyBuilder {
  private val revisions = mutableListOf<VcsChangeSet>()

  public fun revision(changeSet: VcsChangeSet): VcsProxyBuilder {
    revisions += changeSet
    return this
  }

  public operator fun VcsChangeSet.unaryPlus() {
    revision(this)
  }

  public fun build(): VcsProxy = FakeVcsProxy(revisions)
}

public fun vcsProxy(init: Init<VcsProxyBuilder>): VcsProxy = VcsProxyBuilder().apply(init).build()
