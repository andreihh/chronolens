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

package org.chronolens.core.subprocess

import org.junit.Test
import java.io.BufferedReader
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SubprocessTest {
    private val script = javaClass.getResourceAsStream("test_script.py")
        .bufferedReader().use(BufferedReader::readText)

    private fun execute(
        out: String = "",
        err: String = "",
        delaySeconds: Int = 0,
        exitValue: Int = 0
    ): Result = Subprocess.execute(
        "python", "-c", script, out, err, "$delaySeconds", "$exitValue"
    )

    @Test fun `test get output`() {
        val message = "Hello, world!"
        val expected = "$message\n"
        val actual = execute(out = message).get()
        assertEquals(expected, actual)
    }

    @Test fun `test get error`() {
        val expectedMessage = "Hello, error!"
        val expectedExitValue = 1
        val actual = execute(
            err = expectedMessage,
            exitValue = expectedExitValue
        ) as Result.Error
        assertEquals(expectedExitValue, actual.exitValue)
        assertEquals(expectedMessage, actual.message)
    }

    @Test fun `test killed throws`() {
        assertFailsWith<SubprocessException> {
            execute(exitValue = 137) // UNIX SIGKILL
        }
    }

    @Test fun `test interrupt throws`() {
        Thread.currentThread().interrupt()
        assertFailsWith<SubprocessException> {
            while (true) {
                execute(delaySeconds = 1).get()
            }
        }
    }

    @Test fun `test invalid command throws`() {
        assertFailsWith<SubprocessException> {
            Subprocess.execute("non-existing-program")
        }
    }
}
