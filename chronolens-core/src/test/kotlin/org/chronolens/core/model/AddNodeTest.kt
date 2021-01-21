/*
 * Copyright 2018-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.model.sourceTree
import org.junit.Test
import kotlin.test.assertFailsWith

class AddNodeTest {
    @Test fun `test add source file`() {
        val expected = sourceTree {
            sourceFile("src/Main.java") {}
            sourceFile("src/Test.java") {}
        }
        val edit = sourceFile("src/Test.java").add {}

        val actual = sourceTree {
            sourceFile("src/Main.java") {}
        }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test fun `test add function to type`() {
        val expected = sourceTree {
            sourceFile("src/Test.java") {
                type("Test") {
                    function("getVersion()") {}
                }
            }
        }
        val edit = sourceFile("src/Test.java").type("Test")
            .function("getVersion()").add {}

        val actual = sourceTree {
            sourceFile("src/Test.java") {
                type("Test") {}
            }
        }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test fun `test add type with function with parameter`() {
        val expected = sourceTree {
            sourceFile("src/Test.java") {
                type("Test") {
                    function("getV(String)") {
                        parameters("name")
                    }
                }
            }
        }
        val edit = sourceFile("src/Test.java").type("Test").add {
            function("getV(String)") {
                parameters("name")
            }
        }

        val actual = sourceTree {
            sourceFile("src/Test.java") {}
        }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test fun `test add existing node throws`() {
        val sourceTree = sourceTree {
            sourceFile("src/Test.java") {
                type("Test") {}
            }
        }
        val edit = sourceFile("src/Test.java").type("Test").add {}

        assertFailsWith<IllegalStateException> {
            sourceTree.apply(edit)
        }
    }

    /*@Test fun `test add entity to variable throws`() {
        val project = project {
            sourceFile("src/Test.java") {
                variable("version") {}
            }
        }
        val edit = sourceFile("src/Test.java")
            .variable("version").function("getVersion()")
            .add {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }*/

    @Test fun `test add node to non-existing parent throws`() {
        val sourceTree = sourceTree {
            sourceFile("src/Main.java") {}
        }
        val edit = sourceFile("src/Main.java").type("Main")
            .function("getVersion()").add {}

        assertFailsWith<IllegalStateException> {
            sourceTree.apply(edit)
        }
    }
}
