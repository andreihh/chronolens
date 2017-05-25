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

import org.metanalysis.core.subprocess.Result.Error
import org.metanalysis.core.subprocess.Subprocess.execute

import java.io.InterruptedIOException

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class SubprocessTest {
    @Test fun `test get echo message`() {
        val message = "Hello, world!"
        val expected = "$message\n"
        val actual = execute("echo", message).get()
        assertEquals(expected, actual)
    }

    @Test fun `test date with invalid argument returns error`() {
        val actual = execute("date", "invalid-date") as Error
        assertNotEquals(0, actual.exitValue)
    }

    @Test fun `test interrupt find throws`() {
        Thread.currentThread().interrupt()
        assertFailsWith<InterruptedIOException> {
            while (true) {
                execute("find").get()
            }
        }
    }
}
