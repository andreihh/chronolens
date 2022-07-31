/*
 * Copyright 2017-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.model

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.chronolens.test.core.model.sourceFile
import org.junit.Test

class SourceNodeTest {
    @Test
    fun `test duplicated entity in source file throws`() {
        val entities =
            setOf(
                Type(name = Identifier("Type"), modifiers = setOf("abstract")),
                Type(name = Identifier("Type"), modifiers = setOf("interface"))
            )
        assertFailsWith<IllegalArgumentException> {
            SourceFile(path = "src/Test.java", entities = entities)
        }
    }

    @Test
    fun `test duplicated entity in type throws`() {
        val members =
            setOf(
                Type(name = Identifier("InnerType"), modifiers = setOf("abstract")),
                Type(name = Identifier("InnerType"), modifiers = setOf("interface"))
            )
        assertFailsWith<IllegalArgumentException> {
            Type(name = Identifier("Type"), members = members)
        }
    }

    @Test
    fun `test duplicated parameter in function throws`() {
        val signature = "getVersion(int, int)"
        val parameters = listOf("param", "param")
        assertFailsWith<IllegalArgumentException> {
            Function(signature = Signature(signature), parameters = parameters)
        }
    }

    @Test
    fun `test children of source file are equal to entities`() {
        val source = sourceFile("src/Test.java").build { type("Test") {} }
        assertEquals(source.entities, source.children)
    }

    @Test
    fun `test children of type are equal to members`() {
        val type = sourceFile("src/Test.java").type("Test").build { variable("version") {} }
        assertEquals(type.members, type.children)
    }

    @Test
    fun `test children of function is empty collection`() {
        val function =
            sourceFile("src/Test.java").function("getVersion(String)").build { parameters("name") }
        assertTrue(function.children.isEmpty())
    }

    @Test
    fun `test children of variable is empty collection`() {
        val variable = sourceFile("src/Test.java").variable("VERSION").build {}
        assertTrue(variable.children.isEmpty())
    }
}
