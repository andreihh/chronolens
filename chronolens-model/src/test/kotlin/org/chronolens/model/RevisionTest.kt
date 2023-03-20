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

import kotlin.test.assertEquals
import org.chronolens.test.model.add
import org.chronolens.test.model.edit
import org.chronolens.test.model.remove
import org.chronolens.test.model.revision
import org.junit.Test

class RevisionTest {
  @Test
  fun changeSet_returnsTouchedFiles() {
    val expected = setOf("Main.java", "Test.java", "MainTest.java").map(::SourcePath).toSet()

    val actual =
      revision("123") {
          +SourcePath("Main.java").type("Main").type("MainType").add {}
          +SourcePath("Main.java").type("Main").type("MainType").edit {}
          +SourcePath("Test.java").type("Test").type("TestType").remove()
          +SourcePath("MainTest.java").type("MainTest").edit {}
        }
        .changeSet

    assertEquals(expected, actual)
  }
}
