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

package org.metanalysis.core.model

import org.junit.Test

import org.metanalysis.core.model.SourceNode.SourceEntity.Function
import org.metanalysis.core.model.SourceNode.SourceEntity.Type
import org.metanalysis.core.model.SourceNode.SourceEntity.Variable
import org.metanalysis.core.model.SourceNode.SourceUnit

import kotlin.test.assertEquals

class UtilsTest {
    @Test fun `test children of unit are equal to entities`() {
        val unit = SourceUnit(
                id = "src/Test.java",
                entities = setOf(Type("src/Test.java:Test"))
        )

        assertEquals(unit.entities, unit.children.toSet())
    }

    @Test fun `test children of type are equal to members`() {
        val type = Type(
                id = "src/Test.java:Test",
                members = setOf(Variable("src/Test.java:Test:version"))
        )

        assertEquals(type.members, type.children.toSet())
    }

    @Test fun `test children of function are equal to parameters`() {
        val function = Function(
                id = "src/Test.java:getVersion(String)",
                parameters = listOf(
                        Variable("src/Test.java:getVersion(String):name")
                )
        )

        assertEquals(function.parameters.toSet(), function.children.toSet())
    }

    @Test fun `test children of variable are equal to empty collection`() {
        val variable = Variable(id = "src/Test.java:VERSION")

        assertEquals(emptySet(), variable.children.toSet())
    }
}
