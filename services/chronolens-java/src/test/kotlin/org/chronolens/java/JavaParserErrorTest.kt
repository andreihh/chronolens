/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.java

import kotlin.test.assertEquals
import org.chronolens.core.model.typeModifierOf
import org.chronolens.core.parsing.Result
import org.chronolens.test.core.model.type
import org.chronolens.test.core.model.variable
import org.junit.Test

class JavaParserErrorTest : JavaParserTest() {
    @Test
    fun `test parse invalid source is syntax error`() {
        val source = "cla Main { int i = &@*; { class K; interface {}"
        val result = parseResult(source)
        assertEquals(Result.SyntaxError, result)
    }

    @Test
    fun `test parse malformed type is syntax error`() {
        val source = "class 2Main {}"
        val result = parseResult(source)
        assertEquals(Result.SyntaxError, result)
    }

    @Test
    fun `test parse malformed method is syntax error`() {
        val source = "class Main { void 2setName() { in k = 0; } }"
        val result = parseResult(source)
        assertEquals(Result.SyntaxError, result)
    }

    @Test
    fun `test parse malformed modifiers is syntax error`() {
        val source = "class interface Main {}"
        val result = parseResult(source)
        assertEquals(Result.SyntaxError, result)
    }

    @Test
    fun `test parse duplicated members in class is syntax error`() {
        val source = "class Main { int i = 2; int i = 3; }"
        val result = parseResult(source)
        assertEquals(Result.SyntaxError, result)
    }

    @Test
    fun `test parse duplicated parameters in method is syntax error`() {
        val source = "class Main { void setName(String name, int name) {} }"
        val result = parseResult(source)
        assertEquals(Result.SyntaxError, result)
    }

    @Test
    fun `test initializers are ignored`() {
        val source =
            """
        class Type {
            int i;
            {
                i = 2;
            }
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("Type") {
                modifiers("class")
                +variable("i") { modifiers(typeModifierOf("int")) }
            }
        }
        val result = parseResult(source)
        assertEquals(Result.Success(expected), result)
    }
}
