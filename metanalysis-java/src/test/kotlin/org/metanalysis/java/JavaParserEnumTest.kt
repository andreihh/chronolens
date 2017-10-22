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
import kotlin.test.assertEquals

class JavaParserEnumTest : JavaParserTest() {
    @Test fun `test enum`() {
        val source = """
        enum Color {
        }
        """.trimIndent()
        val expected = sourceUnit {
            type("Color") {
                modifiers("enum")
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test fun `test enum with constants`() {
        val source = """
        enum Color {
            RED,
            GREEN,
            BLUE;
        }
        """.trimIndent()
        val expected = sourceUnit {
            type("Color") {
                modifiers("enum")
                variable("RED") { +"RED" }
                variable("GREEN") { +"GREEN" }
                variable("BLUE") { +"BLUE" }
            }
        }
        assertEquals(expected, parse(source))
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
        """.trimIndent()
        val expected = sourceUnit {
            type("Color") {
                modifiers("enum")
                variable("RED") { +"RED" }
                variable("GREEN") { +"GREEN" }
                variable("BLUE") { +"BLUE" }
                variable("format") {
                    modifiers("public", "final")

                    +"\"hex\""
                }
                variable("i") {
                    modifiers("public", "static")
                }
            }
        }
        assertEquals(expected, parse(source))
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
        """.trimIndent()
        val expected = sourceUnit {
            type("Color") {
                modifiers("enum")

                variable("RED") {
                    +"RED() {"
                    +"@Override String getCode() {"
                    +"return \"#FF0000\";"
                    +"}"
                    +"}"
                }

                variable("GREEN") {
                    +"GREEN() {"
                    +"@Override String getCode() {"
                    +"return \"#00FF00\";"
                    +"}"
                    +"}"
                }

                variable("BLUE") {
                    +"BLUE() {"
                    +"@Override String getCode() {"
                    +"return \"#0000FF\";"
                    +"}"
                    +"}"
                }

                function("getCode()") {
                    modifiers("abstract")
                }
            }
        }
        assertEquals(expected, parse(source))
    }

}
