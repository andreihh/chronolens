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

package org.chronos.core.delta

import org.chronos.core.Node.Variable
import org.chronos.core.delta.FunctionChange.ParameterChange

import org.junit.Test

import kotlin.test.assertEquals

class ParameterChangeTest {
    @Test(expected = IndexOutOfBoundsException::class)
    fun `test parameter remove negative index throws`() {
        val parameters = listOf(Variable("name"))
        parameters.apply(ParameterChange.Remove(-1))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `test parameter remove index out of bounds throws`() {
        val parameters = listOf(Variable("name"))
        parameters.apply(ParameterChange.Remove(1))
    }

    @Test fun `test parameter remove`() {
        val expected = emptyList<Variable>()
        val actual = listOf(Variable("name"))
                .apply(ParameterChange.Remove(0))
        assertEquals(expected, actual)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `test parameter add negative index throws`() {
        val parameters = listOf(Variable("name"))
        parameters.apply(ParameterChange.Add(-1, Variable("version")))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `test parameter add index out of bounds throws`() {
        val parameters = listOf(Variable("name"))
        parameters.apply(ParameterChange.Add(2, Variable("version")))
    }

    @Test fun `test parameter add`() {
        val expected = listOf(Variable("version"), Variable("name"))
        val actual = listOf(Variable("name"))
                .apply(ParameterChange.Add(0, Variable("version")))
        assertEquals(expected, actual)
    }
}
