/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.cli

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.cli.ArgParser
import org.chronolens.api.analysis.option

class CommandLineOptionsProviderTest {
  private enum class TestEnum {
    VALUE_A,
    VALUE_B,
    VALUE_C
  }

  private val parser = ArgParser("test")
  private val optionsProvider = CommandLineOptionsProvider(parser)

  @Test
  fun nullableOption_whenProvidedValue_returnsValue() {
    val option = optionsProvider.option<Int>().name("test-opt").description("test").nullable()

    parser.parse(arrayOf("--test-opt", "42"))

    assertEquals(expected = 42, actual = option.getValue())
  }

  @Test
  fun nullableOption_whenNoProvidedValue_returnsNull() {
    val option = optionsProvider.option<Int>().name("test-opt").description("test").nullable()

    parser.parse(arrayOf())

    assertNull(option.getValue())
  }

  @Test
  fun requiredOption_whenProvidedValue_returnsValue() {
    val option = optionsProvider.option<Int>().name("test-opt").description("test").required()

    parser.parse(arrayOf("--test-opt", "42"))

    assertEquals(expected = 42, actual = option.getValue())
  }

  @Test
  fun repeatedOption_whenProvidedValues_returnsList() {
    val option = optionsProvider.option<Int>().name("test-opt").description("test").repeated()

    parser.parse(arrayOf("--test-opt", "42", "--test-opt", "24", "--test-opt", "4224"))

    assertEquals(expected = listOf(42, 24, 4224), actual = option.getValue())
  }

  @Test
  fun repeatedOption_whenNoProvidedValues_returnsEmptyList() {
    val option = optionsProvider.option<Int>().name("test-opt").description("test").repeated()

    parser.parse(arrayOf())

    assertEquals(expected = emptyList(), actual = option.getValue())
  }

  @Test
  fun booleanOption_whenProvided_returnsTrue() {
    val option =
      optionsProvider.option<Boolean>().name("test-opt").description("test").defaultValue(false)

    parser.parse(arrayOf("--test-opt"))

    assertTrue(option.getValue())
  }

  @Test
  fun booleanOption_whenNotProvided_returnsFalse() {
    val option =
      optionsProvider.option<Boolean>().name("test-opt").description("test").defaultValue(false)

    parser.parse(arrayOf())

    assertFalse(option.getValue())
  }

  @Test
  fun intOption_returnsValue() {
    val option = optionsProvider.option<Int>().name("test-opt").description("test").required()

    parser.parse(arrayOf("--test-opt", "42"))

    assertEquals(expected = 42, actual = option.getValue())
  }

  @Test
  fun doubleOption_returnsValue() {
    val option = optionsProvider.option<Double>().name("test-opt").description("test").required()

    parser.parse(arrayOf("--test-opt", "42.5"))

    assertEquals(expected = 42.5, actual = option.getValue(), absoluteTolerance = 1e-6)
  }

  @Test
  fun stringOption_returnsValue() {
    val option = optionsProvider.option<String>().name("test-opt").description("test").required()

    parser.parse(arrayOf("--test-opt", "hello"))

    assertEquals(expected = "hello", actual = option.getValue())
  }

  @Test
  fun enumOption_returnsValue() {
    val option = optionsProvider.option<TestEnum>().name("test-opt").description("test").repeated()

    parser.parse(arrayOf("--test-opt", "VALUE_A", "--test-opt", "VALUE_B", "--test-opt", "VALUE_C"))

    assertEquals(
      expected = listOf(TestEnum.VALUE_A, TestEnum.VALUE_B, TestEnum.VALUE_C),
      actual = option.getValue(),
    )
  }

  @Test
  fun invalidOptionType_throws() {
    assertFails { optionsProvider.option<File>().name("test-opt").description("test").nullable() }
  }
}
