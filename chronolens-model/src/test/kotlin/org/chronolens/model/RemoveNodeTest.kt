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
import org.chronolens.test.model.function
import org.chronolens.test.model.remove
import org.chronolens.test.model.sourceFile
import org.chronolens.test.model.sourceTree
import org.chronolens.test.model.type
import org.chronolens.test.model.variable
import org.junit.Test

class RemoveNodeTest {
  @Test
  fun apply_withRemovedSourceFile_removesSourceNode() {
    val expected = sourceTree { +sourceFile("src/Test.java") {} }
    val edit = SourcePath("src/Main.java").remove()

    val actual = sourceTree {
      +sourceFile("src/Main.java") {
        +type("Main") { +function("getVersion(String)") { parameters("name") } }
      }
      +sourceFile("src/Test.java") {}
    }
    actual.apply(edit)

    assertEquals(expected, actual)
  }

  @Test
  fun apply_withRemovedFunctionFromTypeInSourceFile_removesSourceNode() {
    val expected = sourceTree {
      +sourceFile("src/Main.java") { +type("Main") { +variable("version") {} } }
    }
    val edit = SourcePath("src/Main.java").type("Main").function("getVersion(String)").remove()

    val actual = sourceTree {
      +sourceFile("src/Main.java") {
        +type("Main") {
          +variable("version") {}
          +function("getVersion(String)") { parameters("name") }
        }
      }
    }
    actual.apply(edit)

    assertEquals(expected, actual)
  }

  @Test
  fun apply_withNonExistingId_throws() {
    val sourceTree = sourceTree { +sourceFile("src/Test.java") { +function("getVersion()") {} } }
    val edit = SourcePath("src/Test.java").variable("version").remove()

    assertFailsWith<IllegalStateException> { sourceTree.apply(edit) }
  }
}
