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

import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.project
import org.chronolens.test.core.model.sourceFile
import org.junit.Test
import kotlin.test.assertFailsWith

class EditFunctionTest {
    @Test fun `test edit invalid function id throws`() {
        assertFailsWith<IllegalArgumentException> {
            EditFunction("src/Test.java#getVersion)")
        }
    }

    @Test fun `test add modifier to function`() {
        val expected = project {
            sourceFile("src/Test.java") {
                type("Test") {
                    function("getVersion()") {
                        modifiers("@Override")
                    }
                }
            }
        }
        val edit = sourceFile("src/Test.java").type("Test")
            .function("getVersion()").edit {
                modifiers { +"@Override" }
            }

        val actual = project {
            sourceFile("src/Test.java") {
                type("Test") {
                    function("getVersion()") {}
                }
            }
        }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test fun `test swap function parameters`() {
        val expected = project {
            sourceFile("src/Test.java") {
                function("getValue(int, int)") {
                    parameters("x", "y")
                }
            }
        }
        val edit = sourceFile("src/Test.java")
            .function("getValue(int, int)").edit {
                parameters {
                    remove(0)
                    add(index = 1, value = "y")
                }
            }

        val actual = project {
            sourceFile("src/Test.java") {
                function("getValue(int, int)") {
                    parameters("y", "x")
                }
            }
        }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test fun `test edit non-existing function throws`() {
        val project = project {
            sourceFile("src/Test.java") {
                function("get_version()") {}
            }
        }
        val edit = sourceFile("src/Test.java").function("getVersion()").edit {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }

    @Test fun `test edit non-existing parameter throws`() {
        val project = project {
            sourceFile("src/Test.java") {
                function("getValue(int, int)") {
                    parameters("y", "x")
                }
            }
        }
        val edit = sourceFile("src/Test.java")
            .function("getValue(int, int)").edit {
                parameters {
                    remove(2)
                }
            }

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }
}
