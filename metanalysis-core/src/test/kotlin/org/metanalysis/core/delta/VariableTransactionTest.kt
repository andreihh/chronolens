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
import org.metanalysis.test.core.model.assertEquals

import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class VariableTransactionTest {
    private fun assertDiff(src: Variable, dst: Variable) {
        assertEquals(src.apply(src.diff(dst)), dst)
        assertEquals(dst.apply(dst.diff(src)), src)
    }

    @Test fun `test change modifiers`() {
        val name = "version"
        val expected = Variable(name, modifiers = setOf("public"))
        val actual = Variable(name, modifiers = setOf("private"))
                .apply(VariableTransaction(modifierEdits = listOf(
                        SetEdit.Remove("private"),
                        SetEdit.Add("public")
                )))
        assertEquals(expected, actual)
    }

    @Test fun `test change initializer`() {
        val name = "version"
        val expected = Variable(name, initializer = listOf("1"))
        val actual = Variable(name, initializer = listOf("2"))
                .apply(VariableTransaction(initializerEdits = listOf(
                        ListEdit.Remove(0),
                        ListEdit.Add(0, "1")
                )))
        assertEquals(expected, actual)
    }

    @Test fun `test apply null transaction returns equal variable`() {
        val variable = Variable("version", initializer = listOf("1"))
        assertEquals(variable, variable.apply(transaction = null))
    }

    @Test fun `test diff equal variables returns null`() {
        val variable = Variable("version", initializer = listOf("1"))
        assertNull(variable.diff(variable))
    }

    @Test fun `test diff variables with different identifiers throws`() {
        assertFailsWith<IllegalArgumentException> {
            Variable("version").diff(Variable("version2"))
        }
    }

    @Test fun `test diff modifiers`() {
        val name = "name"
        val src = Variable(name, modifiers = setOf("public"))
        val dst = Variable(name, modifiers = setOf("private"))
        assertDiff(src, dst)
    }

    @Test fun `test diff initializer`() {
        val name = "name"
        val src = Variable(name, initializer = listOf("1"))
        val dst = Variable(name, initializer = listOf("2"))
        assertDiff(src, dst)
    }
}
