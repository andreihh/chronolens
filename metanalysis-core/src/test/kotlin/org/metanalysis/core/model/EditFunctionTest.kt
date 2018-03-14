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

package org.metanalysis.core.model

import org.junit.Test
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.editFunction
import org.metanalysis.test.core.model.project
import kotlin.test.assertFailsWith

class EditFunctionTest {
    @Test fun `test edit invalid function id throws`() {
        assertFailsWith<IllegalArgumentException> {
            EditFunction("src/Test.java:getVersion)")
        }
    }

    @Test fun `test add modifier to function`() {
        val expected = project {
            sourceUnit("src/Test.java") {
                type("Test") {
                    function("getVersion()") {
                        modifiers("@Override")
                    }
                }
            }
        }

        val actual = project {
            sourceUnit("src/Test.java") {
                type("Test") {
                    function("getVersion()") {}
                }
            }
        }
        actual.apply(editFunction("src/Test.java:Test:getVersion()") {
            modifiers { +"@Override" }
        })

        assertEquals(expected, actual)
    }

    @Test fun `test swap function parameters`() {
        val expected = project {
            sourceUnit("src/Test.java") {
                function("getValue(int, int)") {
                    parameters("x", "y")
                }
            }
        }

        val actual = project {
            sourceUnit("src/Test.java") {
                function("getValue(int, int)") {
                    parameters("y", "x")
                }
            }
        }
        actual.apply(editFunction("src/Test.java:getValue(int, int)") {
            parameters {
                remove(0)
                add(index = 1, value = "y")
            }
        })

        assertEquals(expected, actual)
    }

    @Test fun `test edit non-existing function throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                function("get_version()") {}
            }
        }
        val edit = editFunction("src/Test.java:getVersion()") {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }

    @Test fun `test edit non-existing parameter throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                function("getValue(int, int)") {
                    parameters("y", "x")
                }
            }
        }
        val edit = editFunction("src/Test.java:getValue(int, int)") {
            parameters {
                remove(2)
            }
        }

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }
}
