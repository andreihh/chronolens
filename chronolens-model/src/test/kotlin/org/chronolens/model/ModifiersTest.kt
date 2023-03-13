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
import kotlin.test.assertNull
import org.chronolens.test.model.function
import org.chronolens.test.model.variable
import org.junit.Test

class ModifiersTest {
  @Test
  fun `test return type`() {
    val returnType = "int"
    val function =
      function("getVersion()") { modifiers("public", returnTypeModifierOf(returnType)) }
    assertEquals(returnType, function.returnType)
  }

  @Test
  fun `test null return type`() {
    val function = function("getVersion()") {}
    assertNull(function.returnType)
  }

  @Test
  fun `test variable type`() {
    val variableType = "String"
    val variable = variable("VERSION") { modifiers("public", typeModifierOf(variableType)) }
    assertEquals(variableType, variable.type)
  }

  @Test
  fun `test null variable type`() {
    val variable = variable("VERSION") {}
    assertNull(variable.type)
  }
}
