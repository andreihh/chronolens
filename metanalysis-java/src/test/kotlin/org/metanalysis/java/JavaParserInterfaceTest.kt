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

class JavaParserInterfaceTest : JavaParserTest() {
    @Test fun `test interface`() {
        val source = """
        interface IInterface {
        }
        """
        val expected = sourceFileOf(Type(name = "IInterface"))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test interface with fields`() {
        val source = """
        interface IInterface {
            String name = null;
            int version = 1;
        }
        """
        val expected = sourceFileOf(Type(name = "IInterface", members = setOf(
                Variable(name = "name", initializer = listOf("null")),
                Variable(name = "version", initializer = listOf("1"))
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
        val expected = sourceFileOf(Type(name = "IInterface", members = setOf(
                Function(signature = "getName()"),
                Function(signature = "getVersion()")
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
        val expected = sourceFileOf(Type(name = "IInterface", members = setOf(
                Function(signature = "getName()"),
                Function(
                        signature = "getVersion()",
                        modifiers = setOf("default"),
                        body = """{
                            return 1;
                        }""".toBlock()
                )
        )))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test interface with supertypes`() {
        val source = """
        interface IInterface extends Comparable<IInterface> {
        }
        """
        val expected = sourceFileOf(Type(
                name = "IInterface",
                supertypes = setOf("Comparable<IInterface>")
        ))
        assertEquals(expected, parser.parse(source))
    }
}
