/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.interactive

import kotlin.test.Test
import kotlin.test.assertEquals
import org.chronolens.test.core.model.function
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.model.type
import org.chronolens.test.core.model.variable

class PrettyPrinterTest {
    @Test
    fun print_emptyVariable() {
        val expected = "variable VERSION\n"

        val sourceNode = variable("VERSION") {}

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_variableWithSingleModifier() {
        val expected =
            ("""
            variable VERSION
            `- modifiers:
               `- public

            """)
                .trimIndent()

        val sourceNode = variable("VERSION") { modifiers("public") }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_variableWithMultipleModifiers() {
        val expected =
            ("""
            variable VERSION
            `- modifiers:
               |- public
               `- final

            """)
                .trimIndent()

        val sourceNode = variable("VERSION") { modifiers("public", "final") }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_variableWithSingleLineInitializer() {
        val expected =
            ("""
            variable VERSION
            `- initializer: "123"

            """)
                .trimIndent()

        val sourceNode = variable("VERSION") { +"123" }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_variableWithMultipleLineInitializer() {
        val expected =
            ("""
            variable VERSION
            `- initializer: "123 + " ... (trimmed)

            """)
                .trimIndent()

        val sourceNode =
            variable("VERSION") {
                +"123 + "
                +"456"
            }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_variableWithMultipleModifiersAndMultipleLineInitializer() {
        val expected =
            ("""
            variable VERSION
            |- modifiers:
            |  |- public
            |  |- static
            |  `- final
            `- initializer: "123 + " ... (trimmed)

            """)
                .trimIndent()

        val sourceNode =
            variable("VERSION") {
                modifiers("public", "static", "final")
                +"123 + "
                +"  456 + "
                +"  789"
            }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_emptyFunction() {
        val expected = "function main(String[])\n"

        val sourceNode = function("main(String[])") {}

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_functionWithSingleParameter() {
        val expected =
            ("""
            function main(String[])
            `- parameters:
               `- args

            """)
                .trimIndent()

        val sourceNode = function("main(String[])") { parameters("args") }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_functionWithMultipleParameters() {
        val expected =
            ("""
            function main(int, String[])
            `- parameters:
               |- argSize
               `- args

            """)
                .trimIndent()

        val sourceNode = function("main(int, String[])") { parameters("argSize", "args") }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_functionWithSingleLineBody() {
        val expected =
            ("""
            function main(String[])
            `- body: "{}"

            """)
                .trimIndent()

        val sourceNode = function("main(String[])") { +"{}" }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_functionWithMultipleLineBody() {
        val expected =
            ("""
            function main(String[])
            `- body: "{" ... (trimmed)

            """)
                .trimIndent()

        val sourceNode =
            function("main(String[])") {
                +"{"
                +"}"
            }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_functionWithMultipleParametersAndMultipleLineBody() {
        val expected =
            ("""
            function main(int, int, String[])
            |- parameters:
            |  |- n
            |  |- argSize
            |  `- args
            `- body: "{" ... (trimmed)

            """)
                .trimIndent()

        val sourceNode =
            function("main(int, int, String[])") {
                parameters("n", "argSize", "args")
                +"{"
                +"System.out.println(n);"
                +"}"
            }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_emptyType() {
        val expected = "type Main\n"

        val sourceNode = type("Main") {}

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_typeWithSingleSupertype() {
        val expected =
            ("""
            type Main
            `- supertypes:
               `- Object

            """)
                .trimIndent()

        val sourceNode = type("Main") { supertypes("Object") }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_typeWithMultipleSupertypes() {
        val expected =
            ("""
            type Main
            `- supertypes:
               |- Comparable<Main>
               `- Object

            """)
                .trimIndent()

        val sourceNode = type("Main") { supertypes("Comparable<Main>", "Object") }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_typeWithSingleModifier() {
        val expected =
            ("""
            type Main
            `- modifiers:
               `- public

            """)
                .trimIndent()

        val sourceNode = type("Main") { modifiers("public") }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_typeWithMultipleModifiers() {
        val expected =
            ("""
            type Main
            `- modifiers:
               |- public
               `- final

            """)
                .trimIndent()

        val sourceNode = type("Main") { modifiers("public", "final") }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_typeWithSingleMember() {
        val expected =
            ("""
            type Main
            `- members:
               `- variable VERSION

            """)
                .trimIndent()

        val sourceNode = type("Main") { +variable("VERSION") {} }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_typeWithMultipleMembers() {
        val expected =
            ("""
            type Main
            `- members:
               |- variable VERSION
               `- function main(String[])

            """)
                .trimIndent()

        val sourceNode =
            type("Main") {
                +variable("VERSION") {}
                +function("main(String[])") {}
            }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_typeWithMultipleSupertypesAndModifiersAndMembers() {
        val expected =
            ("""
            type Main
            |- supertypes:
            |  |- Comparable<Main>
            |  |- Serializable
            |  `- Object
            |- modifiers:
            |  |- public
            |  |- final
            |  `- class
            `- members:
               |- variable VERSION
               |  |- modifiers:
               |  |  |- public
               |  |  |- static
               |  |  `- final
               |  `- initializer: "123" ... (trimmed)
               |- function main(int, int, String[])
               |  |- parameters:
               |  |  |- n
               |  |  |- argSize
               |  |  `- args
               |  |- modifiers:
               |  |  |- public
               |  |  |- static
               |  |  `- void
               |  `- body: "{" ... (trimmed)
               `- type InnerType
                  |- supertypes:
                  |  |- Object
                  |  |- Serializable
                  |  `- Comparable<InnerType>
                  `- modifiers:
                     |- public
                     |- static
                     `- class

            """)
                .trimIndent()

        val sourceNode =
            type("Main") {
                supertypes("Comparable<Main>", "Serializable", "Object")
                modifiers("public", "final", "class")
                +variable("VERSION") {
                    modifiers("public", "static", "final")
                    +"123"
                    +"+ 456"
                    +"+ 789"
                }
                +function("main(int, int, String[])") {
                    parameters("n", "argSize", "args")
                    modifiers("public", "static", "void")
                    +"{"
                    +"System.out.println(n);"
                    +"}"
                }
                +type("InnerType") {
                    supertypes("Object", "Serializable", "Comparable<InnerType>")
                    modifiers("public", "static", "class")
                }
            }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_emptySourceFile() {
        val expected = "file src/Main.java\n"

        val sourceNode = sourceFile("src/Main.java") {}

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_sourceFileWithSingleEntity() {
        val expected =
            ("""
            file src/Main.java
            `- variable VERSION

            """)
                .trimIndent()

        val sourceNode = sourceFile("src/Main.java") { +variable("VERSION") {} }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }

    @Test
    fun print_sourceFileWithMultipleEntities() {
        val expected =
            ("""
            file src/Main.java
            |- variable VERSION
            `- function main(String[])

            """)
                .trimIndent()

        val sourceNode =
            sourceFile("src/Main.java") {
                +variable("VERSION") {}
                +function("main(String[])") {}
            }

        assertEquals(expected, PrettyPrinter.stringify(sourceNode))
    }
}
