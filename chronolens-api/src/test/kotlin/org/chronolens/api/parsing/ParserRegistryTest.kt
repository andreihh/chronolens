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

package org.chronolens.api.parsing

import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.chronolens.model.SourcePath
import org.junit.Test

class ParserRegistryTest {
  @Test
  fun canParse_languageWithProvidedParser_returnsTrue() {
    assertTrue(Parser.Registry.canParse(SourcePath("Test.fake")))
  }

  @Test
  fun canParse_unknownLanguage_returnsFalse() {
    assertFalse(Parser.Registry.canParse(SourcePath("file.mp3")))
  }

  @Test
  fun tryParse_sourceWithErrors_returnsSyntaxError() {
    val path = SourcePath("res.fake")
    val rawSource = "{ \"invalid\":2"

    assertIs<ParseResult.SyntaxError>(Parser.Registry.tryParse(path, rawSource))
  }

  @Test
  fun tryParse_sourceWithUnsupportedPath_returnsSyntaxError() {
    val path = SourcePath("res.mock")
    val rawSource =
      """
            {
              "@class": "SourceFile",
              "path": "res.mock"
            }
        """
        .trimIndent()

    assertIs<ParseResult.SyntaxError>(Parser.Registry.tryParse(path, rawSource))
  }

  @Test
  fun tryParse_sourceWithDifferentPathThrows() {
    val path = SourcePath("Test.fake")
    val rawSource =
      """
            {
              "@class": "SourceFile",
              "path": "Main.fake"
            }
        """
        .trimIndent()

    assertFailsWith<IllegalStateException> { Parser.Registry.tryParse(path, rawSource) }
  }
}
