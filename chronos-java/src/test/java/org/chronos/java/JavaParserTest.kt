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

package org.chronos.java

import org.chronos.core.Node
import org.chronos.core.SourceFile
import org.junit.Assert.assertEquals
import org.junit.Test

class JavaParserTest {
    val parser = JavaParser()

    @Test fun `test annotation`() {
        val source = """
        @interface AnnotationClass {
        }
        """
        val expected = SourceFile(Node.Type(name = "AnnotationClass"))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test annotation with members`() {
        val source = """
        @interface AnnotationClass {
            String name();
            int version();
        }
        """
        val expected = SourceFile(Node.Type(
                name = "AnnotationClass",
                members = setOf(
                        Node.Variable(name = "name"),
                        Node.Variable(name = "version")
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
        val expected = SourceFile(Node.Type(
                name = "AnnotationClass",
                members = setOf(
                        Node.Variable(name = "name"),
                        Node.Variable(name = "version", initializer = "1")
                )
        ))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test enum`() {
        val source = """
        enum Color {
        }
        """
        val expected = SourceFile(Node.Type(name = "Color"))
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
        val expected = SourceFile(Node.Type(
                name = "Color",
                members = setOf(
                        Node.Variable(name = "RED"),
                        Node.Variable(name = "GREEN"),
                        Node.Variable(name = "BLUE")
                )
        ))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test interface`() {
        val source = """
        interface IInterface {
        }
        """
        val expected = SourceFile(Node.Type(name = "IInterface"))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test interface with fields`() {
        val source = """
        interface IInterface {
            String name = null;
            int version = 1;
        }
        """
        val expected = SourceFile(Node.Type(
                name = "IInterface",
                members = setOf(
                        Node.Variable(name = "name", initializer = "null"),
                        Node.Variable(name = "version", initializer = "1")
                )
        ))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test integration`() {
        val source = javaClass.getResource("/IntegrationTest.java")
        PrettyPrinterVisitor().visit(parser.parse(source))
    }
}
