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

package org.metanalysis.java

import org.junit.Test

import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.java.JavaParser.Companion.toBlock
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.sourceFileOf

class JavaParserEnumTest : JavaParserTest() {
    @Test fun `test enum`() {
        val source = """
        enum Color {
        }
        """
        val expected = sourceFileOf(Type("Color"))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test enum with constants`() {
        val source = """
        enum Color {
            RED,
            GREEN,
            BLUE;
        }
        """
        val expected = sourceFileOf(Type("Color", members = setOf(
                Variable("RED", initializer = listOf("RED")),
                Variable("GREEN", initializer = listOf("GREEN")),
                Variable("BLUE", initializer = listOf("BLUE"))
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test enum with fields`() {
        val source = """
        enum Color {
            RED,
            GREEN,
            BLUE;

            public final String format = "hex";
            public static int i;
        }
        """
        val expected = sourceFileOf(Type("Color", members = setOf(
                Variable("RED", initializer = listOf("RED")),
                Variable("GREEN", initializer = listOf("GREEN")),
                Variable("BLUE", initializer = listOf("BLUE")),
                Variable(
                        name = "format",
                        modifiers = setOf("public", "final"),
                        initializer = listOf("\"hex\"")
                ),
                Variable("i", modifiers = setOf("public", "static"))
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test enum with anonymous class constants`() {
        val source = """
        enum Color {
            RED() {
                @Override String getCode() {
                    return "#FF0000";
                }
            },
            GREEN() {
                @Override String getCode() {
                    return "#00FF00";
                }
            },
            BLUE() {
                @Override String getCode() {
                    return "#0000FF";
                }
            };

            abstract String getCode();
        }
        """
        val expected = sourceFileOf(Type("Color", members = setOf(
                Variable("RED", initializer = """RED() {
                    @Override String getCode() {
                        return "#FF0000";
                    }
                }""".toBlock()),
                Variable("GREEN", initializer = """GREEN() {
                    @Override String getCode() {
                        return "#00FF00";
                    }
                }""".toBlock()),
                Variable("BLUE", initializer = """BLUE() {
                    @Override String getCode() {
                        return "#0000FF";
                    }
                }""".toBlock()),
                Function("getCode()", modifiers = setOf("abstract"))
        )))
        assertEquals(expected, parser.parse(source))
    }

}
