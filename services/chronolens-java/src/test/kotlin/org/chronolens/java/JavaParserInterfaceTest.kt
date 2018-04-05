/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.returnTypeModifierOf
import org.chronolens.core.model.typeModifierOf
import org.junit.Test
import kotlin.test.assertEquals

class JavaParserInterfaceTest : JavaParserTest() {
    @Test fun `test interface`() {
        val source = """
        interface IInterface {
        }
        """.trimIndent()
        val expected = sourceFile {
            type("IInterface") {
                modifiers("interface")
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test fun `test interface with fields`() {
        val source = """
        interface IInterface {
            String name = null;
            int version = 1;
        }
        """.trimIndent()
        val expected = sourceFile {
            type("IInterface") {
                modifiers("interface")
                variable("name") {
                    modifiers(typeModifierOf("String"))
                    +"null"
                }
                variable("version") {
                    modifiers(typeModifierOf("int"))
                    +"1"
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test fun `test interface with methods`() {
        val source = """
        interface IInterface {
            String getName();
            int getVersion();
        }
        """.trimIndent()
        val expected = sourceFile {
            type("IInterface") {
                modifiers("interface")
                function("getName()") {
                    modifiers(returnTypeModifierOf("String"))
                }
                function("getVersion()") {
                    modifiers(returnTypeModifierOf("int"))
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test fun `test interface with default methods`() {
        val source = """
        interface IInterface {
            String getName();
            default int getVersion() {
                return 1;
            }
        }
        """.trimIndent()
        val expected = sourceFile {
            type("IInterface") {
                modifiers("interface")
                function("getName()") {
                    modifiers(returnTypeModifierOf("String"))
                }
                function("getVersion()") {
                    modifiers("default", returnTypeModifierOf("int"))

                    +"{"
                    +"return 1;"
                    +"}"
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test fun `test interface with supertypes`() {
        val source = """
        interface IInterface extends Comparable<IInterface> {
        }
        """.trimIndent()
        val expected = sourceFile {
            type("IInterface") {
                modifiers("interface")
                supertypes("Comparable<IInterface>")
            }
        }
        assertEquals(expected, parse(source))
    }
}
