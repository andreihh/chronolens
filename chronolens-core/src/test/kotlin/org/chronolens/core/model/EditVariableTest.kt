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
import org.chronolens.test.core.model.editVariable
import org.chronolens.test.core.model.project
import org.junit.Test
import kotlin.test.assertFailsWith

class EditVariableTest {
    @Test fun `test edit invalid variable id throws`() {
        assertFailsWith<IllegalArgumentException> {
            EditVariable("src/Test.java:version()")
        }
    }

    @Test fun `test add modifier to variable`() {
        val expected = project {
            sourceFile("src/Test.java") {
                variable("name") {
                    modifiers("@NotNull")
                }
            }
        }

        val actual = project {
            sourceFile("src/Test.java") {
                variable("name") {}
            }
        }
        actual.apply(editVariable("src/Test.java:name") {
            modifiers { +"@NotNull" }
        })

        assertEquals(expected, actual)
    }

    @Test fun `test edit non-existing variable throws`() {
        val project = project {
            sourceFile("src/Test.java") {
                variable("DEBUG") {}
            }
        }
        val edit = editVariable("src/Test.java:RELEASE") {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }

    @Test fun `test edit type throws`() {
        val project = project {
            sourceFile("src/Test.java") {
                type("Test") {}
            }
        }
        val edit = editVariable("src/Test.java:Test") {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }
}
