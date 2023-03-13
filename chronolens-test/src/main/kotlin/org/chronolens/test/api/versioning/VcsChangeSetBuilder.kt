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

import org.chronolens.test.BuilderMarker
import org.chronolens.test.Init
import org.chronolens.test.apply

public typealias VcsChangeSet = Map<String, String?>

public fun VcsChangeSet.touches(path: String): Boolean = this.keys.any { it.startsWith(path) }

@BuilderMarker
public class VcsChangeSetBuilder {
  private val changeSet = mutableMapOf<String, String?>()

  public fun change(sourcePath: String, content: String): VcsChangeSetBuilder {
    changeSet[sourcePath] = content
    return this
  }

  public fun delete(sourcePath: String): VcsChangeSetBuilder {
    changeSet[sourcePath] = null
    return this
  }

  public fun build(): VcsChangeSet {
    require(changeSet.isNotEmpty()) { "VCS change set must contain at least one change!" }
    return changeSet
  }
}

public fun vcsRevision(init: Init<VcsChangeSetBuilder>): VcsChangeSet =
  VcsChangeSetBuilder().apply(init).build()
