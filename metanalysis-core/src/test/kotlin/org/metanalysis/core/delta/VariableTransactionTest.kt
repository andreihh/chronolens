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

import org.metanalysis.core.delta.Transaction.Companion.apply
import org.metanalysis.core.delta.VariableTransaction.Companion.diff
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.test.assertEquals

import kotlin.test.assertNull

class VariableTransactionTest {
    private fun assertDiff(src: Variable, dst: Variable) {
        assertEquals(src.apply(src.diff(dst)), dst)
        assertEquals(dst.apply(dst.diff(src)), src)
    }

    @Test fun `test change initializer`() {
        val name = "version"
        val expected = Variable(name, listOf("1"))
        val actual = Variable(name, listOf("2")).apply(VariableTransaction(
                initializerEdits = listOf(
                        ListEdit.Remove(0),
                        ListEdit.Add(0, "1")
                )
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test apply null transaction returns equal variable`() {
        val variable = Variable("version", listOf("1"))
        assertEquals(variable, variable.apply(transaction = null))
    }

    @Test fun `test diff equal variables returns null`() {
        val variable = Variable("version", listOf("1"))
        assertNull(variable.diff(variable))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test diff variables with different identifiers throws`() {
        Variable("version").diff(Variable("version2"))
    }

    @Test fun `test diff`() {
        val name = "name"
        val src = Variable(name, listOf("1"))
        val dst = Variable(name, listOf("2"))
        assertDiff(src, dst)
    }
}
