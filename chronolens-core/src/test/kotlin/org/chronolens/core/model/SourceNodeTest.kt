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

package org.chronolens.core.model

import org.chronolens.test.core.model.sourceFile
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SourceNodeTest {
    @Test fun `test duplicated entity in source file throws`() {
        val id = "src/Test.java"
        val entities = setOf(
            Type(id = "$id:Type", modifiers = setOf("abstract")),
            Type(id = "$id:Type", modifiers = setOf("interface"))
        )
        assertFailsWith<IllegalArgumentException> {
            SourceFile(id = id, entities = entities)
        }
    }

    @Test fun `test invalid entity id in source file throws`() {
        val id = "src/Test.java"
        val entities = setOf(Type("src/Test:Type"))
        assertFailsWith<IllegalArgumentException> {
            SourceFile(id = id, entities = entities)
        }
    }

    @Test fun `test duplicated entity in type throws`() {
        val id = "src/Test.java:Type"
        val members = setOf(
            Type(id = "$id:InnerType", modifiers = setOf("abstract")),
            Type(id = "$id:InnerType", modifiers = setOf("interface"))
        )
        assertFailsWith<IllegalArgumentException> {
            Type(id = id, members = members)
        }
    }

    @Test fun `test invalid entity id in type throws`() {
        val id = "src/Test.java:Type"
        val members = setOf(Type("src/Test.java:type"))
        assertFailsWith<IllegalArgumentException> {
            Type(id = id, members = members)
        }
    }

    @Test fun `test duplicated parameter in function throws`() {
        val id = "src/Test.java#getVersion(int, int)"
        val parameters = listOf("param", "param")
        assertFailsWith<IllegalArgumentException> {
            Function(id = id, parameters = parameters)
        }
    }

    @Test fun `test path equals source file id`() {
        val path = "src/Test.java"
        val id = "src/Test.java"
        val source = SourceFile(id)
        assertEquals(path, source.path)
    }

    @Test fun `test name equals simple type id`() {
        val name = "Type"
        val id = "src/Test.java:$name"
        val type = Type(id)
        assertEquals(name, type.name)
    }

    @Test fun `test signature equals simple function id`() {
        val signature = "getVersion(int)"
        val id = "src/Test.java#$signature"
        val function = Function(id)
        assertEquals(signature, function.signature)
    }

    @Test fun `test name equals simple variable id`() {
        val name = "VERSION"
        val id = "src/Test.java#$name"
        val variable = Variable(id)
        assertEquals(name, variable.name)
    }

    @Test fun `test children of source file are equal to entities`() {
        val source = sourceFile("src/Test.java").build {
            type("Test") {}
        }
        assertEquals(source.entities, source.children)
    }

    @Test fun `test children of type are equal to members`() {
        val type = sourceFile("src/Test.java").type("Test").build {
            variable("version") {}
        }
        assertEquals(type.members, type.children)
    }

    @Test fun `test children of function is empty collection`() {
        val function = sourceFile("src/Test.java")
            .function("getVersion(String)").build {
                parameters("name")
            }
        assertTrue(function.children.isEmpty())
    }

    @Test fun `test children of variable is empty collection`() {
        val variable = sourceFile("src/Test.java").variable("VERSION").build {}
        assertTrue(variable.children.isEmpty())
    }
}
