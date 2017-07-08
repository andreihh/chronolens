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

import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.sourceFileOf

class JavaParserAnnotationTest : JavaParserTest() {
    @Test fun `test annotation`() {
        val source = """
        @interface AnnotationClass {
        }
        """
        val expected = sourceFileOf(Type(name = "AnnotationClass"))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test annotation with members`() {
        val source = """
        @interface AnnotationClass {
            String name();
            int version();
        }
        """
        val expected = sourceFileOf(Type(
                name = "AnnotationClass",
                members = setOf(
                        Variable(name = "name"),
                        Variable(name = "version")
                )
        ))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test annotation with default members`() {
        val source = """
        @interface AnnotationClass {
            String name();
            int version() default 1;
        }
        """
        val expected = sourceFileOf(Type(
                name = "AnnotationClass",
                members = setOf(
                        Variable(name = "name"),
                        Variable(name = "version", initializer = listOf("1"))
                )
        ))
        assertEquals(expected, parser.parse(source))
    }
}
