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
import org.chronolens.test.core.model.qualifiedPathOf
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.model.sourceTree
import org.chronolens.test.core.model.type
import org.chronolens.test.core.model.variable
import org.junit.Test

class EditTypeTest {
    @Test
    fun newEditType_withInvalidId_throws() {
        assertFailsWith<IllegalArgumentException> { EditType("src/Test.java") }
    }

    @Test
    fun apply_withNewSupertype_addsSupertypeToSourceNode() {
        val expected = sourceTree {
            +sourceFile("src/Test.java") { +type("Test") { supertypes("Object") } }
        }
        val edit = qualifiedPathOf("src/Test.java").type("Test").edit { supertypes { +"Object" } }

        val actual = sourceTree { +sourceFile("src/Test.java") { +type("Test") {} } }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test
    fun apply_withNonExistingId_throws() {
        val sourceTree = sourceTree { +sourceFile("src/Test.java") {} }
        val edit = qualifiedPathOf("src/Test.java").type("Test").edit {}

        assertFailsWith<IllegalStateException> { sourceTree.apply(edit) }
    }

    @Test
    fun apply_withVariableId_throws() {
        val sourceTree = sourceTree { +sourceFile("src/Test.java") { +variable("test") {} } }
        val edit = qualifiedPathOf("src/Test.java").type("test").edit {}

        assertFailsWith<IllegalStateException> { sourceTree.apply(edit) }
    }
}
