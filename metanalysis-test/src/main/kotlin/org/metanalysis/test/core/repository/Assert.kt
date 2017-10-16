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

@file:JvmName("Assert")

package org.metanalysis.test.core.repository

import org.metanalysis.core.repository.Repository
import org.metanalysis.test.core.model.assertEquals

import kotlin.test.assertEquals

fun assertEquals(
        expected: Repository?,
        actual: Repository?,
        message: String? = null
) {
    assertEquals(expected?.getHeadId(), actual?.getHeadId(), message)
    assertEquals(expected?.listSources(), actual?.listSources(), message)
    assertEquals(expected?.getSnapshot(), actual?.getSnapshot(), message)
    assertEquals(
            expected = expected?.getHistory()?.toList(),
            actual = actual?.getHistory()?.toList(),
            message = message
    )
}
