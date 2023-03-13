/*
 * Copyright 2017-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.api.process

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProcessResultTest {
  @Test
  fun get_whenSuccess_returnsOutput() {
    val output = "success"

    val result = ProcessResult.Success(output)

    assertEquals(output, result.get())
  }

  @Test
  fun get_whenError_throws() {
    val result = ProcessResult.Error(exitValue = 1, message = "failure")

    assertFailsWith<ProcessException> { result.get() }
  }

  @Test
  fun getOrNull_whenSuccess_returnsOutput() {
    val output = "success"

    val result = ProcessResult.Success(output)

    assertEquals(output, result.getOrNull())
  }

  @Test
  fun getOrNull_whenError_returnsNull() {
    val result = ProcessResult.Error(exitValue = 1, message = "failure")

    assertNull(result.getOrNull())
  }

  @Test
  fun isSuccess_whenSuccess_returnsTrue() {
    val result = ProcessResult.Success("success")

    assertTrue(result.isSuccess)
  }

  @Test
  fun isSuccess_whenError_returnsFalse() {
    val result = ProcessResult.Error(exitValue = 1, message = "failure")

    assertFalse(result.isSuccess)
  }
}
