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

import org.chronos.core.Node.Function
import org.chronos.core.Node.Variable
import org.chronos.core.delta.Transaction.Companion.apply
import org.chronos.test.assertEquals

import org.junit.Test

class FunctionTransactionTest {
    @Test fun `test add parameter`() {
        val signature = "println(String)"
        val parameter = Variable("s")
        val expected = Function(signature, listOf(parameter))
        val actual = Function(signature, emptyList()).apply(FunctionTransaction(
                parameterEdits = listOf(ListEdit.Add(0, parameter))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test remove parameter`() {
        val signature = "println()"
        val expected = Function(signature, emptyList())
        val actual = Function(signature, listOf(Variable("s"))).apply(
                FunctionTransaction(listOf(ListEdit.Remove(0)), null)
        )
        assertEquals(expected, actual)
    }

    @Test fun `test rename parameter`() {
        val signature = "println(String)"
        val parameter = Variable("s")
        val expected = Function(signature, listOf(parameter))
        val actual = Function(signature, listOf(Variable("t"))).apply(
                FunctionTransaction(listOf(
                        ListEdit.Add(0, parameter),
                        ListEdit.Remove(1)
                ))
        )
        assertEquals(expected, actual)
    }

    @Test fun `test change body`() {
        val signature = "println()"
        val body = "{\n  i = 1;\n}\n"
        val expected = Function(signature, emptyList(), body)
        val actual = Function(signature, emptyList(), "{\n  j = 2;\n}\n").apply(
                FunctionTransaction(emptyList(), BlockEdit.Set(body))
        )
        assertEquals(expected, actual)
    }

    @Test fun `test rename parameter and change body`() {
        val signature = "println(String)"
        val parameter = Variable("s")
        val body = "{\n  i = 1;\n}\n"
        val expected = Function(signature, listOf(parameter), body)
        val actual = Function(
                signature = signature,
                parameters = listOf(Variable("t")),
                body = "{\n  j = 2;\n}\n"
        ).apply(FunctionTransaction(
                parameterEdits = listOf(
                        ListEdit.Add(1, parameter),
                        ListEdit.Remove(0)),
                bodyEdit = BlockEdit.Set(body)
        ))
        assertEquals(expected, actual)
    }
}
