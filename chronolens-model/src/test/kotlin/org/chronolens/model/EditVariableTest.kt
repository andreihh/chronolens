/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.model

import kotlin.test.assertFailsWith
import org.chronolens.model.SourceTreeEdit.Companion.apply
import org.chronolens.test.model.assertEquals
import org.chronolens.test.model.edit
import org.chronolens.test.model.sourceFile
import org.chronolens.test.model.sourceTree
import org.chronolens.test.model.type
import org.chronolens.test.model.variable
import org.junit.Test

class EditVariableTest {
  @Test
  fun apply_withNewModifier_addsModifierToSourceNode() {
    val expected = sourceTree {
      +sourceFile("src/Test.java") { +variable("name") { modifiers("@NotNull") } }
    }
    val edit = SourcePath("src/Test.java").variable("name").edit { modifiers { +"@NotNull" } }

    val actual = sourceTree { +sourceFile("src/Test.java") { +variable("name") {} } }
    actual.apply(edit)

    assertEquals(expected, actual)
  }

  @Test
  fun apply_withNonExistingId_throws() {
    val sourceTree = sourceTree { +sourceFile("src/Test.java") { +variable("DEBUG") {} } }
    val edit = SourcePath("src/Test.java").variable("RELEASE").edit {}

    assertFailsWith<IllegalStateException> { sourceTree.apply(edit) }
  }

  @Test
  fun apply_withTypeId_throws() {
    val sourceTree = sourceTree { +sourceFile("src/Test.java") { +type("Test") {} } }
    val edit = SourcePath("src/Test.java").variable("Test").edit {}

    assertFailsWith<IllegalStateException> { sourceTree.apply(edit) }
  }
}
