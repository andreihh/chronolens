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
import org.metanalysis.core.model.Parser
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.serialization.JsonDriver
import org.metanalysis.test.PrettyPrinterVisitor
import org.metanalysis.test.assertEquals

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

import kotlin.test.assertNotNull

class JavaParserTest {
    private val parser = checkNotNull(Parser.getByExtension("java"))

    @Test(expected = IOException::class)
    fun `test parse non-existent file throws`() {
        parser.parse(File("non-existing-file"))
    }

    @Test(expected = IOException::class)
    fun `test parse non-existing URL throws`() {
        parser.parse(URL("file:///non-existing-url"))
    }

    @Test(expected = IOException::class)
    fun `test parse invalid source throws`() {
        parser.parse("cla Main { int i = &@*; { class K; interface {}")
    }

    @Test fun `test annotation`() {
        val source = """
        @interface AnnotationClass {
        }
        """
        val expected = SourceFile(setOf(Type("AnnotationClass")))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test annotation with members`() {
        val source = """
        @interface AnnotationClass {
            String name();
            int version();
        }
        """
        val expected = SourceFile(setOf(Type(
                name = "AnnotationClass",
                members = setOf(Variable("name"), Variable("version"))
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test annotation with default members`() {
        val source = """
        @interface AnnotationClass {
            String name();
            int version() default 1;
        }
        """
        val expected = SourceFile(setOf(Type(
                name = "AnnotationClass",
                members = setOf(Variable("name"), Variable("version", "1"))
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test enum`() {
        val source = """
        enum Color {
        }
        """
        val expected = SourceFile(setOf(Type("Color")))
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
        val expected = SourceFile(setOf(Type(
                name = "Color",
                members = setOf(
                        Variable("RED", "RED"),
                        Variable("GREEN", "GREEN"),
                        Variable("BLUE", "BLUE")
                )
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
        val expected = SourceFile(setOf(Type(
                name = "Color",
                members = setOf(
                        Variable("RED", "RED"),
                        Variable("GREEN", "GREEN"),
                        Variable("BLUE", "BLUE"),
                        Variable("format", "\"hex\""),
                        Variable("i")
                )
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
        val expected = SourceFile(setOf(Type(
                name = "Color",
                members = setOf(
                        Variable(
                                name = "RED",
                                initializer = """RED() {
                                    @Override String getCode() {
                                        return "#FF0000";
                                    }
                                }"""
                        ),
                        Variable(
                                name = "GREEN",
                                initializer = """GREEN() {
                                    @Override String getCode() {
                                        return "#00FF00";
                                    }
                                }"""
                        ),
                        Variable(
                                name = "BLUE",
                                initializer = """BLUE() {
                                    @Override String getCode() {
                                        return "#0000FF";
                                    }
                                }"""
                        ),
                        Function("getCode()", emptyList())
                )
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test interface`() {
        val source = """
        interface IInterface {
        }
        """
        val expected = SourceFile(setOf(Type("IInterface")))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test interface with fields`() {
        val source = """
        interface IInterface {
            String name = null;
            int version = 1;
        }
        """
        val expected = SourceFile(setOf(Type(
                name = "IInterface",
                members = setOf(
                        Variable("name", "null"),
                        Variable("version", "1")
                )
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test interface with methods`() {
        val source = """
        interface IInterface {
            String getName();
            int getVersion();
        }
        """
        val expected = SourceFile(setOf(Type(
                name = "IInterface",
                members = setOf(
                        Function("getName()", emptyList()),
                        Function("getVersion()", emptyList())
                )
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test interface with default methods`() {
        val source = """
        interface IInterface {
            String getName();
            default int getVersion() {
                return 1;
            }
        }
        """
        val expected = SourceFile(setOf(Type(
                name = "IInterface",
                members = setOf(
                        Function("getName()", emptyList()),
                        Function(
                                signature = "getVersion()",
                                parameters = emptyList(),
                                body = """{
                return 1;
            }"""
                        )
                )
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test interface with supertypes`() {
        val source = """
        interface IInterface extends Comparable<IInterface> {
        }
        """
        val expected = SourceFile(setOf(Type(
                name = "IInterface",
                supertypes = setOf("Comparable<IInterface>")
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test integration`() {
        val source = javaClass.getResource("/IntegrationTest.java")
        PrettyPrinterVisitor().visit(parser.parse(source))
    }

    @Test fun `test file integration`() {
        val source = File("src/test/resources/IntegrationTest.java")
        PrettyPrinterVisitor().visit(assertNotNull(Parser.parseFile(source)))
    }

    @Test fun `test network`() {
        val source = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/master/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
        PrettyPrinterVisitor().visit(parser.parse(source))
    }
}
