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

package org.metanalysis.core.subprocess

import org.junit.Test

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultTest {
    @Test fun `test get success returns input`() {
        val input = "success"
        val result = Result.Success(input)
        assertEquals(input, result.get())
    }

    @Test(expected = SubprocessException::class)
    fun `test get error throws`() {
        val result = Result.Error(1, "failure")
        result.get()
    }

    @Test fun `test get or null success returns input`() {
        val input = "success"
        val result = Result.Success(input)
        assertEquals(input, result.getOrNull())
    }

    @Test fun `test get or null error returns null`() {
        val result = Result.Error(1, "failure")
        assertNull(result.getOrNull())
    }

    @Test fun `test success result is success`() {
        val result = Result.Success("success")
        assertTrue(result.isSuccess)
    }

    @Test fun `test error result is not success`() {
        val result = Result.Error(1, "failure")
        assertFalse(result.isSuccess)
    }
}
