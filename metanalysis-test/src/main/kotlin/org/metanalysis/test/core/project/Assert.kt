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

package org.metanalysis.test.core.project

import org.metanalysis.core.project.Project
import org.metanalysis.test.core.delta.assertEquals
import org.metanalysis.test.core.model.assertEquals

import kotlin.test.assertEquals as assertEqualsKt
import kotlin.test.assertNull

fun assertEquals(
        expected: Project?,
        actual: Project?,
        message: String? = null
) {
    if (expected == null) {
        assertNull(actual, message)
    }
    if (expected != null && actual != null) {
        val expectedFiles = expected.listFiles()
        val actualFiles = actual.listFiles()
        assertEqualsKt(expectedFiles, actualFiles, message)
        expectedFiles.forEach { path ->
            assertEquals(
                    expected = expected.getFileModel(path),
                    actual = actual.getFileModel(path),
                    message = message
            )
            val expectedHistory = expected.getFileHistory(path)
            val actualHistory = actual.getFileHistory(path)
            assertEqualsKt(expectedHistory, actualHistory, message)
            val history = expectedHistory.zip(actualHistory)
            history.forEach { (expectedEntry, actualEntry) ->
                assertEquals(
                        expected = expectedEntry.transaction,
                        actual = actualEntry.transaction,
                        message = message
                )
            }
        }
    }
}
