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

import org.metanalysis.core.model.ProjectEdit.EditVariable
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.project

import kotlin.test.assertFailsWith

class EditVariableTest {
    @Test fun `test add modifier to parameter`() {
        val expected = project {
            sourceUnit("src/Test.java") {
                function("getVersion()") {
                    parameter("name") {
                        modifiers("@NotNull")
                    }
                }
            }
        }

        val actual = project {
            sourceUnit("src/Test.java") {
                function("getVersion()") {
                    parameter("name") {}
                }
            }
        }
        actual.apply(EditVariable(
                id = "src/Test.java:getVersion():name",
                modifierEdits = listOf(SetEdit.Add("@NotNull"))
        ))

        assertEquals(expected, actual)
    }

    @Test fun `test edit non-existing variable throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                variable("DEBUG") {}
            }
        }
        val edit = EditVariable("src/Test.java:RELEASE")

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }

    @Test fun `test edit type throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                type("Test") {}
            }
        }
        val edit = EditVariable("src/Test.java:Test")

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }
}
