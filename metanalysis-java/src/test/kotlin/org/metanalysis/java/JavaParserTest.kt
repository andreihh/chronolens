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
import org.metanalysis.core.model.Parser.SyntaxError
import org.metanalysis.core.model.SourceFile
import org.metanalysis.java.JavaParser.Companion.toBlock
import org.metanalysis.test.PrettyPrinterVisitor
import org.metanalysis.test.assertEquals

import java.net.URL

class JavaParserTest {
    private val parser = JavaParser()

    @Test(expected = SyntaxError::class)
    fun `test parse invalid source throws`() {
        parser.parse("cla Main { int i = &@*; { class K; interface {}")
    }

    @Test(expected = SyntaxError::class)
    fun `test parse duplicated members in class throws`() {
        parser.parse("class Main { int i = 2; int i = 3; }")
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
                members = setOf(
                        Variable("name"),
                        Variable("version", listOf("1"))
                )
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
                        Variable("RED", listOf("RED")),
                        Variable("GREEN", listOf("GREEN")),
                        Variable("BLUE", listOf("BLUE"))
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
                        Variable("RED", listOf("RED")),
                        Variable("GREEN", listOf("GREEN")),
                        Variable("BLUE", listOf("BLUE")),
                        Variable("format", listOf("\"hex\"")),
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
                                }""".toBlock()
                        ),
                        Variable(
                                name = "GREEN",
                                initializer = """GREEN() {
                                    @Override String getCode() {
                                        return "#00FF00";
                                    }
                                }""".toBlock()
                        ),
                        Variable(
                                name = "BLUE",
                                initializer = """BLUE() {
                                    @Override String getCode() {
                                        return "#0000FF";
                                    }
                                }""".toBlock()
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
                        Variable("name", listOf("null")),
                        Variable("version", listOf("1"))
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
                                }""".toBlock()
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

    @Test fun `test class with supertypes`() {
        val source = """
        class IClass extends Object implements Comparable<IInterface> {
        }
        """
        val expected = SourceFile(setOf(Type(
                name = "IClass",
                supertypes = setOf("Object", "Comparable<IInterface>")
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test class with vararg parameter method`() {
        val source = """
        abstract class IClass {
            abstract void println(String... args);
        }
        """
        val expected = SourceFile(setOf(Type(
                name = "IClass",
                members = setOf(Function(
                        signature = "println(String...)",
                        parameters = listOf(Variable("args"))
                ))
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test(expected = SyntaxError::class)
    fun `test initializers not supported`() {
        val source = """
        class Type {
            int i;
            {
                i = 2;
            }
        }
        """
        parser.parse(source)
    }

    @Test fun `test integration`() {
        val source = javaClass.getResource("/IntegrationTest.java").readText()
        PrettyPrinterVisitor().visit(parser.parse(source))
    }

    @Test fun `test network`() {
        val source = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/master/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
                .readText()
        PrettyPrinterVisitor().visit(parser.parse(source))
    }
}
