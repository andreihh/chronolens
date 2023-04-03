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

import org.chronolens.model.SourceTreeEdit.Companion.apply
import org.chronolens.test.model.assertEquals
import org.chronolens.test.model.edit
import org.chronolens.test.model.function
import org.chronolens.test.model.sourceFile
import org.chronolens.test.model.sourceTree
import org.chronolens.test.model.type
import org.chronolens.test.model.variable
import org.junit.Test

class SourceTreeEditTest {
  private fun assertDiff(before: SourceTree, after: SourceTree, edit: SourceTreeEdit) {
    val actualAfter = SourceTree.of(before.sources)
    actualAfter.apply(edit)
    assertEquals(after, actualAfter)
  }

  @Test
  fun `test diff change type modifiers`() {
    val before = sourceTree { +sourceFile("src/Main.java") { +type("Main") {} } }

    val after = sourceTree {
      +sourceFile("src/Main.java") { +type("Main") { modifiers("interface") } }
    }

    val edit = SourcePath("src/Main.java").type("Main").edit { modifiers { +"interface" } }

    assertDiff(before, after, edit)
  }

  @Test
  fun `test diff change type supertypes`() {
    val before = sourceTree { +sourceFile("src/Main.java") { +type("Main") {} } }

    val after = sourceTree {
      +sourceFile("src/Main.java") { +type("Main") { supertypes("Object") } }
    }

    val edit = SourcePath("src/Main.java").type("Main").edit { supertypes { +"Object" } }

    assertDiff(before, after, edit)
  }

  @Test
  fun `test diff change function modifiers`() {
    val before = sourceTree { +sourceFile("src/Test.java") { +function("getVersion()") {} } }

    val after = sourceTree {
      +sourceFile("src/Test.java") { +function("getVersion()") { modifiers("abstract") } }
    }

    val edit =
      SourcePath("src/Test.java").function("getVersion()").edit { modifiers { +"abstract" } }

    assertDiff(before, after, edit)
  }

  @Test
  fun `test diff change function parameters`() {
    val before = sourceTree {
      +sourceFile("src/Test.java") {
        +function("getVersion(String, int)") { parameters("name", "revision") }
      }
    }

    val after = sourceTree {
      +sourceFile("src/Test.java") {
        +function("getVersion(String, int)") { parameters("className", "revision") }
      }
    }

    val edit =
      SourcePath("src/Test.java").function("getVersion(String, int)").edit {
        parameters {
          remove(0)
          add(0, "className")
        }
      }

    assertDiff(before, after, edit)
  }

  @Test
  fun `test diff change function body`() {
    val before = sourceTree {
      +sourceFile("src/Test.java") { +function("getVersion()") { +"DEBUG" } }
    }

    val after = sourceTree {
      +sourceFile("src/Test.java") { +function("getVersion()") { +"RELEASE" } }
    }

    val edit =
      SourcePath("src/Test.java").function("getVersion()").edit {
        body {
          remove(0)
          add(0, "RELEASE")
        }
      }

    assertDiff(before, after, edit)
  }

  @Test
  fun `test diff change variable modifiers`() {
    val before = sourceTree {
      +sourceFile("src/Test.java") { +variable("version") { modifiers("public") } }
    }

    val after = sourceTree { +sourceFile("src/Test.java") { +variable("version") {} } }

    val edit = SourcePath("src/Test.java").variable("version").edit { modifiers { -"public" } }

    assertDiff(before, after, edit)
  }

  @Test
  fun `test diff change variable initializer`() {
    val before = sourceTree { +sourceFile("src/Test.java") { +variable("version") { +"DEBUG" } } }

    val after = sourceTree { +sourceFile("src/Test.java") { +variable("version") { +"RELEASE" } } }

    val edit =
      SourcePath("src/Test.java").variable("version").edit {
        initializer {
          remove(0)
          add(0, "RELEASE")
        }
      }

    assertDiff(before, after, edit)
  }
}
