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
import org.chronolens.core.model.returnTypeModifierOf
import org.chronolens.core.model.typeModifierOf
import org.chronolens.test.core.model.function
import org.chronolens.test.core.model.type
import org.chronolens.test.core.model.variable
import org.junit.Test

class JavaParserClassTest : JavaParserTest() {
    @Test
    fun `test class with supertypes`() {
        val source =
            """
        class IClass extends Object implements Comparable<IInterface> {
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("class")
                supertypes("Object", "Comparable<IInterface>")
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test
    fun `test class with fields`() {
        val source =
            """
        class IClass {
            String name = null, secondName @NotNull [][];
            short version = 1;
            char separator = '.';
            byte magicNum = 0x2f;
            float pi = 3.14f;
            boolean isValid = true;
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("class")
                +variable("name") {
                    modifiers(typeModifierOf("String"))
                    +"null"
                }
                +variable("secondName") { modifiers(typeModifierOf("String[][]")) }
                +variable("version") {
                    modifiers(typeModifierOf("short"))
                    +"1"
                }
                +variable("separator") {
                    modifiers(typeModifierOf("char"))
                    +"'.'"
                }
                +variable("magicNum") {
                    modifiers(typeModifierOf("byte"))
                    +"0x2f"
                }
                +variable("pi") {
                    modifiers(typeModifierOf("float"))
                    +"3.14f"
                }
                +variable("isValid") {
                    modifiers(typeModifierOf("boolean"))
                    +"true"
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test
    fun `test class with vararg parameter method`() {
        val source =
            """
        abstract class IClass {
            @Override
            abstract void println(String... args);
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("abstract", "class")

                +function("println(String...)") {
                    parameters("args")
                    modifiers("@Override", "abstract", returnTypeModifierOf("void"))
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test
    fun `test class with annotation on method parameter`() {
        val source =
            """
        class IClass {
            abstract void m(@Type("a") String @Type("b") [] p [] @Type("c") []);
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("class")

                +function("m(String[][][])") {
                    parameters("p")
                    modifiers("abstract", returnTypeModifierOf("void"))
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test
    fun `test class with annotation on primitive method parameter`() {
        val source =
            """
        class IClass {
            abstract void m(@Type("a") int @Type("b") [] p [] @Type("c") []);
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("class")

                +function("m(int[][][])") {
                    parameters("p")
                    modifiers("abstract", returnTypeModifierOf("void"))
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test
    fun `test class with wildcard in method`() {
        val source =
            """
        class IClass<T> {
            abstract void m(List<@NotNull ?> p);
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("class")

                +function("m(List<?>)") {
                    parameters("p")
                    modifiers("abstract", returnTypeModifierOf("void"))
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test
    fun `test class with lower bounded wildcard in method`() {
        val source =
            """
        class IClass<T, R> {
            abstract void m(List<@NotNull ? super T> p);
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("class")

                +function("m(List<? super T>)") {
                    parameters("p")
                    modifiers("abstract", returnTypeModifierOf("void"))
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test
    fun `test class with upper bounded wildcard in method`() {
        val source =
            """
        class IClass<T> {
            abstract void m(List< @NotNull   ?   extends     T> p);
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("class")

                +function("m(List<? extends T>)") {
                    parameters("p")
                    modifiers("abstract", returnTypeModifierOf("void"))
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test
    fun `test class with name qualified parameter type in method`() {
        val source =
            """
        class IClass {
            abstract void m(java.lang. @NotNull String p);
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("class")

                +function("m(java.lang.String)") {
                    parameters("p")
                    modifiers("abstract", returnTypeModifierOf("void"))
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test
    fun `test class with qualified parameter type in method`() {
        val source =
            """
        class IClass {
            abstract void m(@NotNull org.IClass2<T>. @NotNull InnerClass p);
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("class")

                +function("m(org.IClass2<T>.InnerClass)") {
                    parameters("p")
                    modifiers("abstract", returnTypeModifierOf("void"))
                }
            }
        }
        assertEquals(expected, parse(source))
    }

    @Test
    fun `test class with method with multiple bounds`() {
        val source =
            """
        class IClass {
            static <T extends A & B> void m(T p) {}
            //static <T extends Comparable<? super T>> void m2(T p) {}
        }
        """.trimIndent()
        val expected = sourceFile {
            +type("IClass") {
                modifiers("class")

                +function("m(T)") {
                    parameters("p")
                    modifiers("static", returnTypeModifierOf("void"))
                    +"{}"
                }
            }
        }
        assertEquals(expected, parse(source))
    }
}
