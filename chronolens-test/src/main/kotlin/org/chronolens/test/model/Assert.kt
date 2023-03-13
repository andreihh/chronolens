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

@file:JvmName("Assert")

package org.chronolens.test.model

import kotlin.test.assertEquals as assertEqualsKt
import org.chronolens.model.SourceTree

public fun assertEquals(expected: SourceTree?, actual: SourceTree?, message: String? = null) {
  val expectedSources = expected?.sources?.sortedBy { it.path.toString() }
  val actualSources = actual?.sources?.sortedBy { it.path.toString() }
  assertEqualsKt(expectedSources, actualSources, message)
}

public fun assertEqualSourceTrees(
  expected: SourceTree?,
  actual: SourceTree?,
  message: String? = null
) {
  val expectedSources = expected?.sources?.sortedBy { it.path.toString() }
  val actualSources = actual?.sources?.sortedBy { it.path.toString() }
  assertEqualsKt(expectedSources, actualSources, message)
}
