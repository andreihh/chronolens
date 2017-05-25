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

package org.metanalysis.core.delta

import org.junit.Test

import org.metanalysis.core.delta.FunctionTransaction.Companion.diff
import org.metanalysis.core.delta.Transaction.Companion.apply
import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.test.core.model.assertEquals

import kotlin.test.assertNull

class FunctionTransactionTest {
    private fun assertDiff(src: Function, dst: Function) {
        assertEquals(src.apply(src.diff(dst)), dst)
        assertEquals(dst.apply(dst.diff(src)), src)
    }

    @Test fun `test change parameters`() {
        val signature = "println(String)"
        val variable = Variable("s", emptySet(), listOf("''"))
        val expected = Function(signature, listOf(variable))
        val actual = Function(signature, listOf(Variable("o")))
                .apply(FunctionTransaction(parameterEdits = listOf(
                        ListEdit.Add(0, variable),
                        ListEdit.Remove(1)
                )))
        assertEquals(expected, actual)
    }

    @Test fun `test change modifiers`() {
        val signature = "println()"
        val expected = Function(signature, emptyList(), setOf("public"))
        val actual = Function(signature, emptyList(), setOf("private"))
                .apply(FunctionTransaction(modifierEdits = listOf(
                        SetEdit.Remove("private"),
                        SetEdit.Add("public")
                )))
        assertEquals(expected, actual)
    }

    @Test fun `test change body`() {
        val signature = "println()"
        val body = listOf("{", "  i = 1;", "}")
        val expected = Function(signature, emptyList(), emptySet(), body)
        val actual = Function(signature, body = listOf("{", "  j = 2;", "}"))
                .apply(FunctionTransaction(bodyEdits = listOf(
                        ListEdit.Remove(1),
                        ListEdit.Add(1, "  i = 1;")
                )))
        assertEquals(expected, actual)
    }

    @Test fun `test apply null transaction returns equal function`() {
        val function = Function("getVersion()")
        assertEquals(function, function.apply(transaction = null))
    }

    @Test fun `test diff equal function returns null`() {
        val function = Function("getVersion()")
        assertNull(function.diff(function))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test diff function with different identifiers throws`() {
        Function("getVersion()").diff(Function("get_version()"))
    }

    @Test fun `test diff modifiers`() {
        val signature = "getVersion()"
        val src = Function(signature, modifiers = setOf("public"))
        val dst = Function(signature, modifiers = setOf("private"))
        assertDiff(src, dst)
    }
}
