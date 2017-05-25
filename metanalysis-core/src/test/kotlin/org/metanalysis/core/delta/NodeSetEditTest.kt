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

import org.metanalysis.core.delta.NodeSetEdit.Companion.apply
import org.metanalysis.core.delta.NodeSetEdit.Companion.diff
import org.metanalysis.core.model.Node
import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.test.core.model.assertEquals

import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NodeSetEditTest {
    private fun assertDiff(src: Set<Node>, dst: Set<Node>) {
        assertEquals(dst, src.apply(src.diff(dst)))
        assertEquals(src, dst.apply(dst.diff(src)))
    }

    @Test fun `test add type`() {
        val type = Type("IClass")
        val expected = setOf(type)
        val actual = emptySet<Node>().apply(NodeSetEdit.Add(type))
        assertEquals(expected, actual)
    }

    @Test fun `test add existing type throws`() {
        val type = Type("IClass")
        assertFailsWith<IllegalStateException> {
            setOf(type).apply(NodeSetEdit.Add(type))
        }
    }

    @Test fun `test remove type`() {
        val name = "IClass"
        val expected = setOf(Variable(name))
        val actual = setOf(Type(name), Variable(name))
                .apply(NodeSetEdit.Remove<Type>(name))
        assertEquals(expected, actual)
    }

    @Test fun `test remove non-existing type throws`() {
        assertFailsWith<IllegalStateException> {
            emptySet<Node>().apply(NodeSetEdit.Remove<Type>("IClass"))
        }
    }

    @Test fun `test change type`() {
        val name = "IClass"
        val supertype = "IInterface"
        val function = Function("getField()")
        val expected = setOf(
                Variable(name),
                Type(
                        name = name,
                        supertypes = setOf(supertype),
                        members = setOf(function)
                )
        )
        val actual = setOf(Variable(name), Type(name))
                .apply(NodeSetEdit.Change<Type>(
                        identifier = name,
                        transaction = TypeTransaction(
                                supertypeEdits = listOf(SetEdit.Add(supertype)),
                                memberEdits = listOf(NodeSetEdit.Add(function))
                        )
                ))
        assertEquals(expected, actual)
    }

    @Test fun `test change non-existing type throws`() {
        assertFailsWith<IllegalStateException> {
            emptySet<Node>().apply(NodeSetEdit.Change<Type>(
                    identifier = "IClass",
                    transaction = TypeTransaction()
            ))
        }
    }

    @Test fun `test add variable`() {
        val variable = Variable("field")
        val expected = setOf(variable)
        val actual = emptySet<Node>().apply(NodeSetEdit.Add(variable))
        assertEquals(expected, actual)
    }

    @Test fun `test add existing variable throws`() {
        val variable = Variable("field")
        assertFailsWith<IllegalStateException> {
            setOf(variable).apply(NodeSetEdit.Add(variable))
        }
    }

    @Test fun `test remove variable`() {
        val name = "field"
        val expected = setOf(Type(name))
        val actual = setOf(Type(name), Variable(name))
                .apply(NodeSetEdit.Remove<Variable>(name))
        assertEquals(expected, actual)
    }

    @Test fun `test remove non-existing variable throws`() {
        assertFailsWith<IllegalStateException> {
            emptySet<Node>().apply(NodeSetEdit.Remove<Variable>("field"))
        }
    }

    @Test fun `test change variable`() {
        val name = "field"
        val expected = setOf(
                Type(name),
                Variable(name,  initializer = listOf("1"))
        )
        val actual = setOf(Type(name), Variable(name))
                .apply(NodeSetEdit.Change<Variable>(
                        identifier = name,
                        transaction = VariableTransaction(
                                initializerEdits = listOf(ListEdit.Add(0, "1"))
                        )
                ))
        assertEquals(expected, actual)
    }

    @Test fun `test change non-existing variable throws`() {
        assertFailsWith<IllegalStateException> {
            emptySet<Node>().apply(NodeSetEdit.Change<Variable>(
                    identifier = "field",
                    transaction = VariableTransaction()
            ))
        }
    }

    @Test fun `test add function`() {
        val function = Function("getVersion()")
        val expected = setOf(function)
        val actual = emptySet<Node>().apply(NodeSetEdit.Add(function))
        assertEquals(expected, actual)
    }

    @Test fun `test add existing function throws`() {
        val function = Function("getVersion()")
        assertFailsWith<IllegalStateException> {
            setOf(function).apply(NodeSetEdit.Add(function))
        }
    }

    @Test fun `test remove function`() {
        val signature = "getField()"
        val expected = setOf(Type(signature))
        val actual = setOf(Type(signature), Function(signature))
                .apply(NodeSetEdit.Remove<Function>(signature))
        assertEquals(expected, actual)
    }

    @Test fun `test remove non-existing function throws`() {
        assertFailsWith<IllegalStateException> {
            emptySet<Node>().apply(NodeSetEdit.Remove<Function>("getField()"))
        }
    }

    @Test fun `test change function`() {
        val signature = "getField()"
        val expected = setOf(
                Type(signature),
                Function(signature, body = listOf("{}"))
        )
        val actual = setOf(
                Type(signature),
                Function(signature, parameters = listOf(Variable("name")))
        ).apply(NodeSetEdit.Change<Function>(
                identifier = signature,
                transaction = FunctionTransaction(
                        parameterEdits = listOf(ListEdit.Remove(0)),
                        bodyEdits = listOf(ListEdit.Add(0, "{}"))
                )
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test change non-existing function throws`() {
        assertFailsWith<IllegalStateException> {
            emptySet<Node>().apply(NodeSetEdit.Change<Function>(
                    identifier = "getField()",
                    transaction = FunctionTransaction()
            ))
        }
    }

    @Test fun `test diff equal node sets returns empty list`() {
        val nodeSet = setOf(
                Variable("version"),
                Function("getVersion()"),
                Type("IClass")
        )
        assertTrue(nodeSet.diff(nodeSet).isEmpty())
    }

    @Test fun `test diff`() {
        val src = setOf(
                Function(
                        signature = "getVersion()",
                        parameters = listOf(Variable("version"))
                ),
                Type("IClass", supertypes = setOf("IInterface")),
                Type("IInterface")
        )
        val dst = setOf(
                Variable("version"),
                Function("getVersion()"),
                Type("IClass")
        )
        assertDiff(src, dst)
    }
}
