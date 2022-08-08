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
import org.chronolens.test.core.model.add
import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.function
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.model.sourceTree
import org.chronolens.test.core.model.type
import org.junit.Test

class AddNodeTest {
    @Test
    fun apply_withNewSourceFile_addsSourceNode() {
        val expected = sourceTree {
            +sourceFile("src/Main.java") {}
            +sourceFile("src/Test.java") {}
        }
        val edit = qualifiedPathOf("src/Test.java").add {}

        val actual = sourceTree { +sourceFile("src/Main.java") {} }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test
    fun apply_withNewFunctionInTypeInSourceFile_addsSourceNode() {
        val expected = sourceTree {
            +sourceFile("src/Test.java") { +type("Test") { +function("getVersion()") {} } }
        }
        val edit = qualifiedPathOf("src/Test.java").type("Test").function("getVersion()").add {}

        val actual = sourceTree { +sourceFile("src/Test.java") { +type("Test") {} } }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test
    fun apply_withNewTypeWithFunctionWithParameterInSourceFile_addsSourceNode() {
        val expected = sourceTree {
            +sourceFile("src/Test.java") {
                +type("Test") { +function("getV(String)") { parameters("name") } }
            }
        }
        val edit =
            qualifiedPathOf("src/Test.java").type("Test").add {
                +function("getV(String)") { parameters("name") }
            }

        val actual = sourceTree { +sourceFile("src/Test.java") {} }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test
    fun apply_withExistingId_throws() {
        val sourceTree = sourceTree { +sourceFile("src/Test.java") { +type("Test") {} } }
        val edit = qualifiedPathOf("src/Test.java").type("Test").add {}

        assertFailsWith<IllegalStateException> { sourceTree.apply(edit) }
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

    @Test
    fun apply_withNonExistingParent_throws() {
        val sourceTree = sourceTree { +sourceFile("src/Main.java") {} }
        val edit = qualifiedPathOf("src/Main.java").type("Main").function("getVersion()").add {}

        assertFailsWith<IllegalStateException> { sourceTree.apply(edit) }
    }
}
