/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.core.model

import org.chronolens.core.model.Project
import org.chronolens.core.model.SourceFile
import kotlin.test.assertEquals as assertEqualsKt

public fun assertEquals(
    expected: Project?,
    actual: Project?,
    message: String? = null,
) {
    val expectedSources = expected?.sources?.toList()?.sortedBy(SourceFile::id)
    val actualSources = actual?.sources?.toList()?.sortedBy(SourceFile::id)
    assertEqualsKt(expectedSources, actualSources, message)
}
