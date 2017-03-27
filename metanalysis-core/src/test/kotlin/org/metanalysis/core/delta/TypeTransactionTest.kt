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
import org.metanalysis.core.delta.TypeTransaction.Companion.diff
import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.test.assertEquals

import kotlin.test.assertNull

class TypeTransactionTest {
    private fun assertDiff(src: Type, dst: Type) {
        assertEquals(src.apply(src.diff(dst)), dst)
        assertEquals(dst.apply(dst.diff(src)), src)
    }

    @Test fun `test add supertype`() {
        val typeName = "IClass"
        val supertype = "IInterface"
        val expected = Type(typeName, setOf(supertype))
        val actual = Type(typeName).apply(TypeTransaction(
                supertypeEdits = listOf(SetEdit.Add(supertype)),
                memberEdits = emptyList()
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test remove supertype`() {
        val typeName = "IClass"
        val supertype = "IInterface"
        val expected = Type(typeName)
        val actual = Type(typeName, setOf(supertype)).apply(TypeTransaction(
                supertypeEdits = listOf(SetEdit.Remove(supertype))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test add type`() {
        val typeName = "IClass"
        val addedType = Type("IInterface")
        val expected = Type(name = typeName, members = setOf(addedType))
        val actual = Type(typeName).apply(TypeTransaction(
                memberEdits = listOf(NodeSetEdit.Add(addedType))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test add variable`() {
        val typeName = "IClass"
        val variable = Variable("version")
        val expected = Type(name = typeName, members = setOf(variable))
        val actual = Type(typeName).apply(TypeTransaction(
                memberEdits = listOf(NodeSetEdit.Add(variable))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test add function`() {
        val typeName = "IClass"
        val function = Function("getVersion()", emptyList())
        val expected = Type(name = typeName, members = setOf(function))
        val actual = Type(typeName).apply(TypeTransaction(
                memberEdits = listOf(NodeSetEdit.Add(function))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test remove type`() {
        val typeName = "IClass"
        val removedName = "IInterface"
        val expected = Type(typeName)
        val actual = Type(
                name = typeName,
                members = setOf(Type(removedName))
        ).apply(TypeTransaction(
                memberEdits = listOf(NodeSetEdit.Remove<Type>(removedName))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test remove variable`() {
        val typeName = "IClass"
        val removedName = "version"
        val expected = Type(typeName)
        val actual = Type(
                name = typeName,
                members = setOf(Variable(removedName))
        ).apply(TypeTransaction(
                memberEdits = listOf(NodeSetEdit.Remove<Variable>(removedName))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test remove function`() {
        val typeName = "IClass"
        val removedSignature = "getVersion()"
        val expected = Type(typeName)
        val actual = Type(
                name = typeName,
                members = setOf(Function(removedSignature, emptyList()))
        ).apply(TypeTransaction(
                memberEdits = listOf(
                        NodeSetEdit.Remove<Function>(removedSignature)
                )
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test change type`() {
        val typeName = "IClass"
        val changedName = "IInterface"
        val supertype = "IClass"
        val expected = Type(name = typeName, members = setOf(Type(changedName)))
        val actual = Type(
                name = typeName,
                members = setOf(Type(
                        name = changedName,
                        supertypes = setOf(supertype)
                ))
        ).apply(TypeTransaction(memberEdits = listOf(NodeSetEdit.Change<Type>(
                identifier = changedName,
                transaction = TypeTransaction(supertypeEdits = listOf(
                        SetEdit.Remove(supertype)
                ))
        ))))
        assertEquals(expected, actual)
    }

    @Test fun `test change variable`() {
        val typeName = "IClass"
        val changedName = "version"
        val expected = Type(
                name = typeName,
                members = setOf(Variable(changedName))
        )
        val actual = Type(
                name = typeName,
                members = setOf(Variable(changedName, "1"))
        ).apply(TypeTransaction(
                memberEdits = listOf(NodeSetEdit.Change<Variable>(
                        identifier = changedName,
                        transaction = VariableTransaction(
                                initializerEdits = listOf(ListEdit.Remove(0))
                        )
                ))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test change function`() {
        val typeName = "IClass"
        val changedSignature = "getVersion()"
        val expected = Type(
                name = typeName,
                members = setOf(Function(changedSignature, emptyList()))
        )
        val actual = Type(
                name = typeName,
                members = setOf(Function(changedSignature, emptyList(), "{}"))
        ).apply(TypeTransaction(
                memberEdits = listOf(NodeSetEdit.Change<Function>(
                        identifier = changedSignature,
                        transaction = FunctionTransaction(
                                bodyEdits = listOf(ListEdit.Remove(0))
                        )
                ))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test apply null transaction returns equal type`() {
        val type = Type("IClass", setOf("IInterface"))
        assertEquals(type, type.apply(transaction = null))
    }

    @Test fun `test diff equal types returns null`() {
        val type = Type("IClass", setOf("IInterface"))
        assertNull(type.diff(type))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test diff types with different identifiers throws`() {
        Type("IClass").diff(Type("IInterface"))
    }
}
