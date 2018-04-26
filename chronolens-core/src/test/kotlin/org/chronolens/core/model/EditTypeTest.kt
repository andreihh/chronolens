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

class EditTypeTest {
    @Test fun `test edit invalid type id throws`() {
        assertFailsWith<IllegalArgumentException> {
            EditType("src/Test.java")
        }
    }

    @Test fun `test add supertype to type`() {
        val expected = project {
            sourceFile("src/Test.java") {
                type("Test") {
                    supertypes("Object")
                }
            }
        }
        val edit = sourceFile("src/Test.java").type("Test").edit {
            supertypes { +"Object" }
        }

        val actual = project {
            sourceFile("src/Test.java") {
                type("Test") {}
            }
        }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test fun `test edit non-existing type throws`() {
        val project = project {
            sourceFile("src/Test.java") {}
        }
        val edit = sourceFile("src/Test.java").type("Test").edit {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }

    @Test fun `test edit type applied to variable id throws`() {
        val project = project {
            sourceFile("src/Test.java") {
                variable("test") {}
            }
        }
        val edit = sourceFile("src/Test.java").type("test").edit {}

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }
}
