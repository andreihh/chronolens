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

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.chronolens.model.SourcePath

class ParserRegistryTest {
  @Test
  fun canParse_whenLanguageWithProvidedParser_returnsTrue() {
    assertTrue(Parser.canParse(SourcePath("Test.kts")))
  }

  @Test
  fun canParse_whenUnknownLanguage_returnsFalse() {
    assertFalse(Parser.canParse(SourcePath("file.mp3")))
  }

  @Test
  fun tryParse_whenSourceWithErrors_returnsSyntaxError() {
    val path = SourcePath("res.kts")
    val rawSource = "sourceFile(\"res.kts\") {"

    assertIs<ParseResult.SyntaxError>(Parser.tryParse(path, rawSource))
  }

  @Test
  fun tryParse_whenSourceWithUnsupportedPath_returnsSyntaxError() {
    val path = SourcePath("res.mock")
    val rawSource = "sourceFile(\"res.mock\") {}"

    assertIs<ParseResult.SyntaxError>(Parser.tryParse(path, rawSource))
  }

  @Test
  fun tryParse_whenSourceWithDifferentPath_throws() {
    val path = SourcePath("Test.kts")
    val rawSource = "sourceFile(\"Main.kts\") {}"

    assertFailsWith<IllegalStateException> { Parser.tryParse(path, rawSource) }
  }
}
