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

import org.metanalysis.core.model.ProjectEdit.EditType
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.editType
import org.metanalysis.test.core.model.project

import kotlin.test.assertFailsWith

class EditTypeTest {
    @Test fun `test edit invalid type id throws`() {
        assertFailsWith<IllegalArgumentException> {
            EditType("src/Test.java")
        }
    }

    @Test fun `test add supertype to type`() {
        val expected = project {
            sourceUnit("src/Test.java") {
                type("Test") {
                    supertypes("Object")
                }
            }
        }

        val actual = project {
            sourceUnit("src/Test.java") {
                type("Test") {}
            }
        }
        actual.apply(editType("src/Test.java:Test") {
            supertypes { +"Object" }
        })

        assertEquals(expected, actual)
    }

    @Test fun `test edit non-existing type throws`() {
        val project = project {
            sourceUnit("src/Test.java") {}
        }
        val edit = editType("src/Test.java:Test") {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }

    @Test fun `test edit type applied to variable id throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                variable("test") {}
            }
        }
        val edit = editType("src/Test.java:test") {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }
}
