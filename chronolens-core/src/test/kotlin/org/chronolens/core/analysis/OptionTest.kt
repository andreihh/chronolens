/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.analysis

import org.chronolens.test.core.analysis.OptionHolder
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class OptionTest {
    @Test
    fun validate_memoizesValidatedValue() {
        val option = OptionHolder(5)
        val validatedOption = option.validate {}
        val optionProperty by validatedOption

        assertEquals(optionProperty, 5)
        assertEquals(optionProperty, 5)
        assertEquals(option.timesRetrieved, 1)
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

        assertFailsWith<InvalidOptionException> {
            @Suppress("UNUSED_EXPRESSION")
            optionProperty
        }
    }

    @Test
    fun transform_memoizesTransformedValue() {
        val option = OptionHolder(5)
        val transformedOption = option.transform(Int::toString)
        val optionProperty by transformedOption

        assertEquals(optionProperty, "5")
        assertEquals(optionProperty, "5")
        assertEquals(option.timesRetrieved, 1)
    }

    @Test
    fun transform_whenThrowsIllegalArgumentException_wrapsIntoInvalidOptionException() {
        val option = OptionHolder(5)
        val transformedOption = option.transform { require(false) }
        val optionProperty by transformedOption

        assertFailsWith<InvalidOptionException> {
            @Suppress("UNUSED_EXPRESSION")
            optionProperty
        }
    }

    @Test
    fun transformIfNotNull_whenNonNullValue_memoizesTransformedValue() {
        val option = OptionHolder<Int?>(5)
        val transformedOption = option.transformIfNotNull(Int::toString)
        val optionProperty by transformedOption

        assertEquals(optionProperty, "5")
        assertEquals(optionProperty, "5")
        assertEquals(option.timesRetrieved, 1)
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

        assertFailsWith<InvalidOptionException> {
            @Suppress("UNUSED_EXPRESSION")
            optionProperty
        }
    }

    @Test
    fun requireOption_whenConditionIsTrue_doesNothing() {
        requireOption(true) { "should be ok" }
    }

    @Test
    fun requireOption_whenConditionIsFalse_throwsInvalidOptionException() {
        assertFailsWith<InvalidOptionException> {
            requireOption(false) { "fail" }
        }
    }

    @Test
    fun optionError_throwsInvalidOptionException() {
        assertFailsWith<InvalidOptionException> {
            optionError("fail")
        }
    }
}
