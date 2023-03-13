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
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.chronolens.test.model.function
import org.chronolens.test.model.sourceFile
import org.chronolens.test.model.type
import org.chronolens.test.model.variable
import org.junit.Test

class SourceNodeTest {
  @Test
  fun newSourceFile_withDuplicateEntity_throws() {
    val entities =
      setOf(
        Type(name = Identifier("Type"), modifiers = setOf("abstract")),
        Type(name = Identifier("Type"), modifiers = setOf("interface"))
      )
    assertFailsWith<IllegalArgumentException> {
      SourceFile(path = SourcePath("src/Test.java"), entities = entities)
    }
  }

  @Test
  fun newType_withDuplicateEntity_throws() {
    val members =
      setOf(
        Type(name = Identifier("InnerType"), modifiers = setOf("abstract")),
        Type(name = Identifier("InnerType"), modifiers = setOf("interface"))
      )
    assertFailsWith<IllegalArgumentException> { Type(name = Identifier("Type"), members = members) }
  }

  @Test
  fun newFunction_withDuplicateParameter_throws() {
    val signature = "getVersion(int, int)"
    val parameters = listOf(Identifier("param"), Identifier("param"))

    assertFailsWith<IllegalArgumentException> {
      Function(signature = Signature(signature), parameters = parameters)
    }
  }

  @Test
  fun children_ofSourceFile_areEqualToEntities() {
    val source = sourceFile("src/Test.java") { +type("Test") {} }

    assertEquals(source.entities, source.children)
  }

  @Test
  fun children_ofType_areEqualToMembers() {
    val type = type("Test") { +variable("version") {} }

    assertEquals(type.members, type.children)
  }

  @Test
  fun children_ofFunction_isEmptyCollection() {
    val function = function("getVersion(String)") { parameters("name") }

    assertTrue(function.children.isEmpty())
  }

  @Test
  fun children_ofVariable_isEmptyCollection() {
    val variable = variable("VERSION") {}

    assertTrue(variable.children.isEmpty())
  }
}
