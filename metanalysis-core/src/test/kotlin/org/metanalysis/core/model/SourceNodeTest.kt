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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SourceNodeTest {
    @Test fun `test duplicated entity in unit throws`() {
        val id = "src/Test.java"
        val entities = listOf(Type("$id:type"), Variable("$id:type"))
        assertFailsWith<IllegalArgumentException> {
            SourceUnit(id = id, entities = entities)
        }
    }

    @Test fun `test invalid entity id in unit throws`() {
        val id = "src/Test.java"
        val entities = listOf(Type("src/Test:type"))
        assertFailsWith<IllegalArgumentException> {
            SourceUnit(id = id, entities = entities)
        }
    }

    @Test fun `test duplicated entity in type throws`() {
        val id = "src/Test.java:Type"
        val members = listOf(Type("$id:member"), Variable("$id:member"))
        assertFailsWith<IllegalArgumentException> {
            Type(id = id, members = members)
        }
    }

    @Test fun `test invalid entity id in type throws`() {
        val id = "src/Test.java:Type"
        val members = listOf(Type("src/Test.java:type"))
        assertFailsWith<IllegalArgumentException> {
            Type(id = id, members = members)
        }
    }

    @Test fun `test duplicated parameter in function throws`() {
        val id = "src/Test.java:getVersion(int, int)"
        val parameters = listOf(Variable("$id:param"), Variable("$id:param"))
        assertFailsWith<IllegalArgumentException> {
            Function(id = id, parameters = parameters)
        }
    }

    @Test fun `test invalid parameter id in function throws`() {
        val id = "src/Test.java:getVersion(int, int)"
        val parameters = listOf(Variable("src/Test:getVersion():code"))
        assertFailsWith<IllegalArgumentException> {
            Function(id = id, parameters = parameters)
        }
    }

    @Test fun `test path equals unit id`() {
        val path = "src/Test.java"
        val id = "src/Test.java"
        val unit = SourceUnit(id)
        assertEquals(path, unit.path)
    }

    @Test fun `test name equals simple type id`() {
        val name = "Type"
        val id = "src/Test.java:$name"
        val type = Type(id)
        assertEquals(name, type.name)
    }

    @Test fun `test signature equals simple function id`() {
        val signature = "getVersion(int)"
        val id = "src/Test.java:$signature"
        val function = Function(id)
        assertEquals(signature, function.signature)
    }
}
