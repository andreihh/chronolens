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

package org.chronolens.api.analysis

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.chronolens.api.analysis.Option.Companion.constrainTo
import org.chronolens.api.analysis.Option.Companion.optionError
import org.chronolens.api.analysis.Option.Companion.requireOption
import org.chronolens.test.api.analysis.OptionHolder
import org.junit.Test

class OptionTest {
  @Test
  fun validate_memoizesValidatedValue() {
    val option = OptionHolder(5)
    val validatedOption = option.validate {}
    val optionProperty by validatedOption

    assertEquals(expected = 5, actual = optionProperty)
    assertEquals(expected = 5, actual = optionProperty)
    assertEquals(expected = 1, actual = option.timesRetrieved)
  }

  @Test
  fun validate_whenNullValue_doesNotInvokeValidator() {
    val option = OptionHolder<Int?>(null)
    val validatedOption = option.validate { error("fail") }
    val optionProperty by validatedOption

    assertNull(optionProperty)
  }

  @Test
  fun validate_whenThrowsIllegalArgumentException_wrapsIntoInvalidOptionException() {
    val option = OptionHolder(5)
    val validatedOption = option.validate { require(false) }
    val optionProperty by validatedOption

    assertFailsWith<InvalidOptionException> { @Suppress("UNUSED_EXPRESSION") optionProperty }
  }

  @Test
  fun transform_memoizesTransformedValue() {
    val option = OptionHolder(5)
    val transformedOption = option.transform(Int::toString)
    val optionProperty by transformedOption

    assertEquals(expected = "5", actual = optionProperty)
    assertEquals(expected = "5", actual = optionProperty)
    assertEquals(expected = 1, actual = option.timesRetrieved)
  }

  @Test
  fun transform_whenThrowsIllegalArgumentException_wrapsIntoInvalidOptionException() {
    val option = OptionHolder(5)
    val transformedOption = option.transform { require(false) }
    val optionProperty by transformedOption

    assertFailsWith<InvalidOptionException> { @Suppress("UNUSED_EXPRESSION") optionProperty }
  }

  @Test
  fun transformIfNotNull_whenNonNullValue_memoizesTransformedValue() {
    val option = OptionHolder<Int?>(5)
    val transformedOption = option.transformIfNotNull(Int::toString)
    val optionProperty by transformedOption

    assertEquals(expected = "5", actual = optionProperty)
    assertEquals(expected = "5", actual = optionProperty)
    assertEquals(expected = 1, actual = option.timesRetrieved)
  }

  @Test
  fun transformIfNotNull_whenNullValue_doesNotInvokeTransformer() {
    val option = OptionHolder<Int?>(null)
    val transformedOption = option.transformIfNotNull<String> { error("fail") }
    val optionProperty by transformedOption

    assertNull(optionProperty)
  }

  @Test
  fun transformIfNotNull_whenThrowsIllegalArgumentException_wrapsIntoInvalidOptionException() {
    val option = OptionHolder(5)
    val transformedOption = option.transformIfNotNull { require(false) }
    val optionProperty by transformedOption

    assertFailsWith<InvalidOptionException> { @Suppress("UNUSED_EXPRESSION") optionProperty }
  }

  @Test
  fun constrainTo_whenMinAndMaxAreNull_skipsValidationAndReturnsInitialValue() {
    for (value in listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE)) {
      val option = OptionHolder(value)
      val constrainedOption = option.constrainTo()
      val optionProperty by constrainedOption

      assertEquals(expected = value, actual = optionProperty)
    }
  }

  @Test
  fun constrainTo_whenLessThanMin_throwsInvalidOptionException() {
    val option = OptionHolder(5)
    val constrainedOption = option.constrainTo(min = 6)
    val optionProperty by constrainedOption

    assertFailsWith<InvalidOptionException> { @Suppress("UNUSED_EXPRESSION") optionProperty }
  }

  @Test
  fun constrainTo_whenGreaterThanMax_throwsInvalidOptionException() {
    val option = OptionHolder(7)
    val constrainedOption = option.constrainTo(max = 6)
    val optionProperty by constrainedOption

    assertFailsWith<InvalidOptionException> { @Suppress("UNUSED_EXPRESSION") optionProperty }
  }

  @Test
  fun constrainTo_whenValueIsWithinRange_returnsInitialValue() {
    val option = OptionHolder(6)
    val constrainedOption = option.constrainTo(min = 5, max = 7)
    val optionProperty by constrainedOption

    assertEquals(expected = 6, actual = optionProperty)
  }

  @Test
  fun constrainTo_whenValueIsEqualToBoundaries_returnsInitialValue() {
    val option = OptionHolder(6)
    val constrainedOption = option.constrainTo(min = 6, max = 6)
    val optionProperty by constrainedOption

    assertEquals(expected = 6, actual = optionProperty)
  }

  @Test
  fun requireOption_whenConditionIsTrue_doesNothing() {
    requireOption(true) { "should be ok" }
  }

  @Test
  fun requireOption_whenConditionIsFalse_throwsInvalidOptionException() {
    assertFailsWith<InvalidOptionException> { requireOption(false) { "fail" } }
  }

  @Test
  fun optionError_throwsInvalidOptionException() {
    assertFailsWith<InvalidOptionException> { optionError("fail") }
  }
}
