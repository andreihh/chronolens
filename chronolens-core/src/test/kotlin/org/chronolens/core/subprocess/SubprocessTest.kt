/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.chronolens.api.process.ProcessException
import org.chronolens.api.process.ProcessResult
import org.junit.Test

class SubprocessTest {
  @Test
  fun `test get stdout`() {
    val message = "Hello, world!"
    val expected = "$message\n"
    val actual = Subprocess.execute("echo", message).get()
    assertEquals(expected, actual)
  }

  @Test
  fun `test get error`() {
    val message = "Hello, error!"
    val expectedMessage = "$message\n"
    val expectedExitValue = 1

    val script = "echo $message >&2; exit $expectedExitValue"
    val actual = Subprocess.execute("bash", "-c", script) as ProcessResult.Error

    assertEquals(expectedMessage, actual.message)
    assertEquals(expectedExitValue, actual.exitValue)
  }

  @Test
  fun `test killed throws`() {
    assertFailsWith<ProcessException> {
      Subprocess.execute("bash", "-c", "exit 137") // UNIX SIGKILL
    }
  }

  @Test
  fun `test interrupt throws`() {
    Thread.currentThread().interrupt()
    assertFailsWith<ProcessException> {
      while (true) {
        Subprocess.execute("bash", "-c", "sleep 1").get()
      }
    }
  }

  @Test
  fun `test invalid command throws`() {
    assertFailsWith<ProcessException> { Subprocess.execute("non-existing-program") }
  }
}
