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

class SubprocessTest {
    fun delay() {
        var i = 0
        repeat(100000000) {
            i += 1
        }
    }

    @Test fun `test get system date`() {
        val expected = "1970-01-01\n"
        val actual = execute("date", "--date=@0", "+%F").get()
        assertEquals(expected, actual)
    }

    @Test fun `test system date with invalid argument returns error`() {
        val expected = Result.Error(
                exitCode = 1,
                message = "date: unrecognized option '---'\n"
                        + "Try 'date --help' for more information.\n"
        )
        val actual = execute("date", "---")
        assertEquals(expected, actual)
    }

    @Test(expected = SubprocessException::class)
    fun `test get system date with invalid arguments throws`() {
        execute("date", "%F").get()
    }

    @Test(expected = InterruptedException::class)
    fun `test interrupted process throws`() {
        Thread.currentThread().interrupt()
        delay()
        execute("date")
    }
}
