/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import kotlin.test.assertFailsWith
import org.chronolens.core.model.SourceTreeEdit.Companion.apply
import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.edit
import org.chronolens.test.core.model.function
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.model.sourceTree
import org.chronolens.test.core.model.type
import org.junit.Test

class EditFunctionTest {
    @Test
    fun apply_withNewModifier_addsModifierToSourceNode() {
        val expected = sourceTree {
            +sourceFile("src/Test.java") {
                +type("Test") { +function("getVersion()") { modifiers("@Override") } }
            }
        }
        val edit =
            qualifiedPathOf("src/Test.java").type("Test").function("getVersion()").edit {
                modifiers { +"@Override" }
            }

        val actual = sourceTree {
            +sourceFile("src/Test.java") { +type("Test") { +function("getVersion()") {} } }
        }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test
    fun apply_withSwappedParameters_swapParametersForSourceNode() {
        val expected = sourceTree {
            +sourceFile("src/Test.java") {
                +function("getValue(int, int)") { parameters("x", "y") }
            }
        }
        val edit =
            qualifiedPathOf("src/Test.java").function("getValue(int, int)").edit {
                parameters {
                    remove(0)
                    add(index = 1, value = "y")
                }
            }

        val actual = sourceTree {
            +sourceFile("src/Test.java") {
                +function("getValue(int, int)") { parameters("y", "x") }
            }
        }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test
    fun apply_withNonExistingId_throws() {
        val sourceTree = sourceTree {
            +sourceFile("src/Test.java") { +function("get_version()") {} }
        }
        val edit = qualifiedPathOf("src/Test.java").function("getVersion()").edit {}

        assertFailsWith<IllegalStateException> { sourceTree.apply(edit) }
    }

    @Test
    fun apply_withNonExistingParameter_throws() {
        val sourceTree = sourceTree {
            +sourceFile("src/Test.java") {
                +function("getValue(int, int)") { parameters("y", "x") }
            }
        }
        val edit =
            qualifiedPathOf("src/Test.java").function("getValue(int, int)").edit {
                parameters { remove(2) }
            }

        assertFailsWith<IllegalStateException> { sourceTree.apply(edit) }
    }
}
