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

package org.metanalysis.core.versioning

import org.junit.Test
import org.metanalysis.core.versioning.Subprocess.Result

import org.metanalysis.core.versioning.Subprocess.execute

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SubprocessTest {
    @Test fun `test get echo message`() {
        val message = "Hello, world!"
        val expected = "$message\n"
        val actual = execute("echo", message).get()
        assertEquals(expected, actual)
    }

    @Test fun `test get or null echo returns message`() {
        val message = "Hello, world!"
        val expected = "$message\n"
        val actual = execute("echo", message).getOrNull()
        assertEquals(expected, actual)
    }

    @Test fun `test echo is success`() {
        val result = execute("echo", "Hello, world!")
        assertTrue(result.isSuccess)
    }

    @Test fun `test date with invalid argument returns error`() {
        val actual = execute("date", "invalid-date") as Result.Error
        assertNotEquals(0, actual.exitValue)
    }

    @Test fun `test get or null date with invalid argument returns null`() {
        val result = execute("date", "invalid-date").getOrNull()
        assertNull(result)
    }

    @Test fun `test date with invalid argument is not success`() {
        val result = execute("date", "invalid-date")
        assertFalse(result.isSuccess)
    }

    @Test(expected = SubprocessException::class)
    fun `test get date with invalid arguments throws`() {
        execute("date", "invalid-date").get()
    }

    @Test(timeout = 1000, expected = IllegalThreadStateException::class)
    fun `test interrupt find throws`() {
        Thread.currentThread().interrupt()
        while (true) {
            execute("find").get()
        }
    }
}
