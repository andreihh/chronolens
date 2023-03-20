/*
 * Copyright 2022-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QualifiedSourceNodeIdTest {
  @Test
  fun sourcePath_ofSourcePath_returnsPath() {
    val path = SourcePath("src/main/java/Main.java")

    assertEquals(path, path.sourcePath)
  }

  @Test
  fun sourcePath_ofType_returnsRootSourcePath() {
    val path = SourcePath("src/main/java/Main.java")
    val qualifiedId = path.type("Main").type("Inner")

    assertEquals(path, qualifiedId.sourcePath)
  }

  @Test
  fun sourcePath_ofVariable_returnsRootSourcePath() {
    val path = SourcePath("src/main/java/Main.java")
    val qualifiedId = path.type("Main").variable("VERSION")

    assertEquals(path, qualifiedId.sourcePath)
  }

  @Test
  fun sourcePath_ofFunction_returnsRootSourcePath() {
    val path = SourcePath("src/main/java/Main.java")
    val qualifiedId = path.type("Main").function("main()")

    assertEquals(path, qualifiedId.sourcePath)
  }

  @Test
  fun parentId_ofSourceFile_returnsParent() {
    val qualifiedId = SourcePath("src/Main.java")

    assertNull(qualifiedId.parentId)
  }

  @Test
  fun parentId_ofType_returnsParent() {
    val parentId = SourcePath("src/Main.java").type("Main")
    val qualifiedId = parentId.type("Inner")

    assertEquals(parentId, qualifiedId.parentId)
  }

  @Test
  fun parentId_ofVariable_returnsParent() {
    val parentId = SourcePath("src/Main.java").type("Main")
    val qualifiedId = parentId.variable("VERSION")

    assertEquals(parentId, qualifiedId.parentId)
  }

  @Test
  fun parentId_ofFunction_returnsParent() {
    val parentId = SourcePath("src/Main.java").type("Main")
    val qualifiedId = parentId.function("main()")

    assertEquals(parentId, qualifiedId.parentId)
  }

  @Test
  fun name_ofType_returnsTypeName() {
    val qualifiedId = SourcePath("src/Main.java").type("Test")

    assertEquals(Identifier("Test"), qualifiedId.name)
  }

  @Test
  fun name_ofVariable_returnsVariableName() {
    val qualifiedId = SourcePath("src/Main.java").variable("VERSION")

    assertEquals(Identifier("VERSION"), qualifiedId.name)
  }

  @Test
  fun signature_ofFunction_returnsFunctionSignature() {
    val qualifiedId = SourcePath("src/Main.java").function("main()")

    assertEquals(Signature("main()"), qualifiedId.signature)
  }

  @Test
  fun isValid_whenValidId_returnsTrue() {
    val rawQualifiedIds =
      listOf(
        ".dotfile",
        "Main.java",
        "src/.dotfile",
        "src/Main.java",
        "src/Main.java:Main",
        "src/Main.java:Main#main(String[])",
        "src/Main.java:Main#VERSION",
        "src/Main.java:Main:InnerMain",
        "src/Main.java:Main:InnerMain#main(String[])",
        "src/Main.java:Main:InnerMain#VERSION",
      )

    for (rawQualifiedId in rawQualifiedIds) {
      assertTrue(
        QualifiedSourceNodeId.isValid(rawQualifiedId),
        "Id '$rawQualifiedId' should be valid!"
      )
    }
  }

  @Test
  fun isValid_whenInvalidId_returnsFalse() {
    val rawQualifiedIds =
      listOf(
        "",
        "src//Main",
        "src/./Main",
        "src/../Main",
        "/src/Main",
        "src/Main/",
        "src/Main::Main",
        "src/Main:#Main",
        "src/Main.java:Main#VERSION/file",
        "src/Main.java:Main#main()/file",
        "src/Main.java:Main:InnerMain/file",
        "src/Main.java:main():InnerMain",
        "src/Main.java:Main#main():VERSION",
        "src/Main.java:Main#main()#main(String[])",
      )

    for (rawQualifiedId in rawQualifiedIds) {
      assertFalse(
        QualifiedSourceNodeId.isValid(rawQualifiedId),
        "Id '$rawQualifiedId' should be invalid!"
      )
    }
  }

  @Test
  fun parseFrom_whenInputIsQualifiedIdAsString_returnsOriginalQualifiedId() {
    val qualifiedIds =
      listOf(
        SourcePath("src/Main.java"),
        SourcePath("src/Main.java").type("Main"),
        SourcePath("src/Main.java").function("main(String[])"),
        SourcePath("src/Main.java").variable("VERSION"),
        SourcePath("src/Main.java").type("Main").type("InnerMain"),
        SourcePath("src/Main.java").type("Main").function("main(String[])"),
        SourcePath("src/Main.java").type("Main").variable("VERSION"),
      )

    for (qualifiedId in qualifiedIds) {
      assertEquals(qualifiedId, QualifiedSourceNodeId.parseFrom(qualifiedId.toString()))
    }
  }

  @Test
  fun parseFrom_whenInvalidId_throws() {
    val rawQualifiedIds =
      listOf(
        "",
        "src/Main::Main",
        "src/Main:#Main",
        "src/Main:   :Main",
        "src/Main:main():Main",
        "src/Main:main(s",
        "src/Main:main)",
        "src/Main.java:Main#main():VERSION",
        "src/Main.java:Main#main()#main(String[])",
      )

    for (rawQualifiedId in rawQualifiedIds) {
      assertFailsWith<IllegalArgumentException>("Parsing id '$rawQualifiedId' should fail!") {
        QualifiedSourceNodeId.parseFrom(rawQualifiedId)
      }
    }
  }

  @Test
  fun castOrNull_whenValidType_returnsId() {
    val qualifiedId = SourcePath("src/Main.java")

    assertEquals<QualifiedSourceNodeId<*>?>(qualifiedId, qualifiedId.castOrNull<SourceContainer>())
  }

  @Test
  fun castOrNull_whenInvalidType_returnsNull() {
    val qualifiedId = SourcePath("src/Main.java")

    assertNull(qualifiedId.castOrNull<SourceEntity>())
  }

  @Test
  fun cast_whenValidType_returnsId() {
    val qualifiedId = SourcePath("src/Main.java")

    assertEquals<QualifiedSourceNodeId<*>>(qualifiedId, qualifiedId.cast<SourceContainer>())
  }

  @Test
  fun cast_whenInvalidType_throws() {
    val qualifiedId = SourcePath("src/Main.java")

    assertFailsWith<IllegalArgumentException> { qualifiedId.cast<SourceEntity>() }
  }
}
