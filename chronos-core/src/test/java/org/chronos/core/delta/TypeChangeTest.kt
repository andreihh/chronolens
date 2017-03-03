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
import org.chronos.core.Node.Type
import org.chronos.core.Node.Variable
import org.chronos.core.delta.Change.Companion.apply
import org.chronos.core.delta.NodeChange.AddNode
import org.chronos.core.delta.NodeChange.ChangeNode
import org.chronos.core.delta.NodeChange.RemoveNode
import org.chronos.core.delta.TypeChange.SupertypeChange.AddSupertype
import org.chronos.core.delta.TypeChange.SupertypeChange.RemoveSupertype
import org.chronos.test.assertEquals

import org.junit.Test

class TypeChangeTest {
    @Test fun `test add supertype`() {
        val typeName = "IClass"
        val supertype = "IInterface"
        val expected = Type(typeName, setOf(supertype))
        val actual = Type(typeName).apply(TypeChange(
                supertypeChanges = listOf(AddSupertype(supertype)),
                memberChanges = emptyList()
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test remove supertype`() {
        val typeName = "IClass"
        val supertype = "IInterface"
        val expected = Type(typeName)
        val actual = Type(typeName, setOf(supertype)).apply(TypeChange(
                supertypeChanges = listOf(RemoveSupertype(supertype))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test add type`() {
        val typeName = "IClass"
        val addedType = Type("IInterface")
        val expected = Type(name = typeName, members = setOf(addedType))
        val actual = Type(typeName).apply(TypeChange(
                memberChanges = listOf(AddNode(addedType))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test add variable`() {
        val typeName = "IClass"
        val variable = Variable("version")
        val expected = Type(name = typeName, members = setOf(variable))
        val actual = Type(typeName).apply(TypeChange(
                memberChanges = listOf(AddNode(variable))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test add function`() {
        val typeName = "IClass"
        val function = Function("getVersion()", emptyList())
        val expected = Type(name = typeName, members = setOf(function))
        val actual = Type(typeName).apply(TypeChange(
                memberChanges = listOf(AddNode(function))
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
        ).apply(TypeChange(
                memberChanges = listOf(RemoveNode<Type>(removedName))
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
        ).apply(TypeChange(
                memberChanges = listOf(RemoveNode<Variable>(removedName))
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
        ).apply(TypeChange(
                memberChanges = listOf(RemoveNode<Function>(removedSignature))
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
        ).apply(TypeChange(memberChanges = listOf(ChangeNode<Type>(
                identifier = changedName,
                change = TypeChange(supertypeChanges = listOf(
                        RemoveSupertype(supertype)
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
        ).apply(TypeChange(memberChanges = listOf(ChangeNode<Variable>(
                identifier = changedName,
                change = VariableChange(null)
        ))))
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
        ).apply(TypeChange(memberChanges = listOf(ChangeNode<Function>(
                identifier = changedSignature,
                change = FunctionChange(bodyChange = BlockChange.Set(null))
        ))))
        assertEquals(expected, actual)
    }
}
