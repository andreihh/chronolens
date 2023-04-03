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

package org.chronolens.core.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.chronolens.model.ListEdit
import org.chronolens.model.ListEdit.Companion.apply
import org.chronolens.model.SetEdit.Companion.apply
import org.chronolens.model.SourcePath
import org.chronolens.model.SourceTree
import org.chronolens.model.SourceTreeEdit.Companion.apply
import org.chronolens.test.model.assertEqualSourceTrees
import org.chronolens.test.model.build
import org.chronolens.test.model.function
import org.chronolens.test.model.sourceFile
import org.chronolens.test.model.sourceTree
import org.chronolens.test.model.type
import org.chronolens.test.model.variable

class DiffingTest {
  private fun String.apply(edits: List<ListEdit<Char>>): String =
    toList().apply(edits).joinToString(separator = "")

  private fun assertDiff(src: String, dst: String) {
    assertEquals(dst, src.apply(src.toList().diff(dst.toList())))
    assertEquals(src, dst.apply(dst.toList().diff(src.toList())))
  }

  private fun <T> assertDiff(src: Set<T>, dst: Set<T>) {
    assertEquals(dst, src.apply(src.diff(dst)))
    assertEquals(src, dst.apply(dst.diff(src)))
  }

  private fun assertDiff(src: SourceTree, dst: SourceTree) {
    val result = SourceTree.of(src.sources)
    result.apply(result.diff(dst))
    assertEqualSourceTrees(dst, result)
  }

  @Test
  fun diff_betweenLists() {
    val src = "democrats"
    val dst = "republican"
    assertDiff(src, dst)
  }

  @Test
  fun diff_betweenNonEmptyListAndEmptyList() {
    val src = "republican"
    val dst = ""
    assertDiff(src, dst)
  }

  @Test
  fun diff_betweenSets() {
    val src = setOf(1, 2, 5, 6)
    val dst = setOf(1, 3, 4, 6)
    assertDiff(src, dst)
  }

  @Test
  fun diff_nodesWithDifferentIds_throws() {
    val before = SourcePath("src/Test.java").build {}
    val after = SourcePath("src/Main.java").build {}

    assertFailsWith<IllegalArgumentException> { before.diff(after) }
  }

  @Test
  fun diff_betweenSourceTrees() {
    val src = sourceTree {
      +sourceFile("src/Main.java") {
        +type("Main") {
          +function("getVersion(String)") { parameters("name") }
          +variable("VERSION") { modifiers("final", "static") }
          +function("main(String...)") {}
          +variable("innerConflict") {}
          +type("innerConflict") {}
        }
      }
      +sourceFile("src/Test.java") {}
    }

    val dst = sourceTree {
      +sourceFile("src/Main.java") {
        +type("Main") {
          +function("getVersion(String)") { parameters("name") }
          +variable("VERSION") { modifiers("final") }
          +function("main()") {}
          +variable("innerConflict") { modifiers("final") }
          +type("innerConflict") { modifiers("abstract") }
        }
      }
      +sourceFile("src/Test2.java") {}
    }

    assertDiff(src, dst)
  }
}
