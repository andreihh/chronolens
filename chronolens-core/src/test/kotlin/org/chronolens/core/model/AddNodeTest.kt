/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.model

import org.chronolens.test.core.model.addFunction
import org.chronolens.test.core.model.addSourceFile
import org.chronolens.test.core.model.addType
import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.project
import org.junit.Test
import kotlin.test.assertFailsWith

class AddNodeTest {
    @Test fun `test add source file`() {
        val expected = project {
            sourceFile("src/Main.java") {}
            sourceFile("src/Test.java") {}
        }

        val actual = project {
            sourceFile("src/Main.java") {}
        }
        actual.apply(addSourceFile("src/Test.java") {})

        assertEquals(expected, actual)
    }

    @Test fun `test add function to type`() {
        val expected = project {
            sourceFile("src/Test.java") {
                type("Test") {
                    function("getVersion()") {}
                }
            }
        }

        val actual = project {
            sourceFile("src/Test.java") {
                type("Test") {}
            }
        }
        actual.apply(addFunction("src/Test.java:Test#getVersion()") {})

        assertEquals(expected, actual)
    }

    @Test fun `test add type with function with parameter`() {
        val expected = project {
            sourceFile("src/Test.java") {
                type("Test") {
                    function("getV(String)") {
                        parameters("name")
                    }
                }
            }
        }

        val actual = project {
            sourceFile("src/Test.java") {}
        }
        actual.apply(addType("src/Test.java:Test") {
            function("getV(String)") {
                parameters("name")
            }
        })

        assertEquals(expected, actual)
    }

    @Test fun `test add existing node throws`() {
        val project = project {
            sourceFile("src/Test.java") {
                type("Test") {}
            }
        }
        val edit = addType("src/Test.java:Test") {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }

    @Test fun `test add entity to variable throws`() {
        val project = project {
            sourceFile("src/Test.java") {
                variable("version") {}
            }
        }
        val edit = addFunction("src/Test.java:version#getVersion()") {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }

    @Test fun `test add node to non-existing parent throws`() {
        val project = project {
            sourceFile("src/Main.java") {}
        }
        val edit = addFunction("src/Main.java:Main#getVersion()") {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }
}
