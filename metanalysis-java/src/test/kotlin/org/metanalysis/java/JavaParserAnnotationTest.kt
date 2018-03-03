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
import org.metanalysis.core.model.typeModifierOf
import kotlin.test.assertEquals

class JavaParserAnnotationTest : JavaParserTest() {
    @Test fun `test annotation`() {
        val source = """
        @interface AnnotationClass {
        }
        """.trimIndent()
        val expected = sourceUnit {
            type("AnnotationClass") {
                modifiers("@interface")
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test fun `test annotation with members`() {
        val source = """
        @interface AnnotationClass {
            String name();
            int version();
        }
        """.trimIndent()
        val expected = sourceUnit {
            type("AnnotationClass") {
                modifiers("@interface")
                variable("name") {
                    modifiers(typeModifierOf("String"))
                }
                variable("version") {
                    modifiers(typeModifierOf("int"))
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test fun `test annotation with default members`() {
        val source = """
        @interface AnnotationClass {
            String name();
            int version() default 1;
        }
        """.trimIndent()
        val expected = sourceUnit {
            type("AnnotationClass") {
                modifiers("@interface")
                variable("name") {
                    modifiers(typeModifierOf("String"))
                }
                variable("version") {
                    modifiers(typeModifierOf("int"))
                    +"1"
                }
            }
        }
        assertEquals(expected, parse(source))
    }
}
