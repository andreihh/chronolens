/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import org.chronolens.model.returnTypeModifierOf
import org.chronolens.model.typeModifierOf
import org.chronolens.test.model.function
import org.chronolens.test.model.type
import org.chronolens.test.model.variable
import org.junit.Test

class JavaParserEnumTest : JavaParserTest() {
  @Test
  fun `test enum`() {
    val source =
      """
        enum Color {
        }
        """
        .trimIndent()
    val expected = sourceFile { +type("Color") { modifiers("enum") } }
    assertEquals(expected, parse(source))
  }

  @Test
  fun `test enum with constants`() {
    val source =
      """
        enum Color {
            RED,
            GREEN,
            BLUE;
        }
        """
        .trimIndent()
    val expected = sourceFile {
      +type("Color") {
        modifiers("enum")
        +variable("RED") {}
        +variable("GREEN") {}
        +variable("BLUE") {}
      }
    }
    assertEquals(expected, parse(source))
  }

  @Test
  fun `test enum with fields`() {
    val source =
      """
        enum Color {
            RED,
            GREEN,
            BLUE;

            public final String format = "hex";
            public static int i;
        }
        """
        .trimIndent()
    val expected = sourceFile {
      +type("Color") {
        modifiers("enum")
        +variable("RED") {}
        +variable("GREEN") {}
        +variable("BLUE") {}
        +variable("format") {
          modifiers("public", "final", typeModifierOf("String"))

          +"\"hex\""
        }
        +variable("i") { modifiers("public", "static", typeModifierOf("int")) }
      }
    }
    assertEquals(expected, parse(source))
  }

  @Test
  fun `test enum with anonymous class constants`() {
    val source =
      """
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
            /**
             * This is a sample {@code Java=Doc}.
             */
            BLUE() {
                @Override String getCode() {
                    return "#0000FF";
                }
            };

            abstract String getCode();
        }
        """
        .trimIndent()
    val expected = sourceFile {
      +type("Color") {
        modifiers("enum")

        +variable("RED") {
          +"{"
          +"@Override String getCode() {"
          +"return \"#FF0000\";"
          +"}"
          +"}"
        }

        +variable("GREEN") {
          +"{"
          +"@Override String getCode() {"
          +"return \"#00FF00\";"
          +"}"
          +"}"
        }

        +variable("BLUE") {
          +"{"
          +"@Override String getCode() {"
          +"return \"#0000FF\";"
          +"}"
          +"}"
        }

        +function("getCode()") { modifiers("abstract", returnTypeModifierOf("String")) }
      }
    }
    assertEquals(expected, parse(source))
  }

  @Test
  fun `test enum with annotation on constant`() {
    val source =
      """
            enum Color {
                @IntColor
                RED,
                GREEN,
                BLUE
            }
        """
        .trimIndent()
    val expected = sourceFile {
      +type("Color") {
        modifiers("enum")

        +variable("RED") { modifiers("@IntColor") }

        +variable("GREEN") {}
        +variable("BLUE") {}
      }
    }
    assertEquals(expected, parse(source))
  }
}
