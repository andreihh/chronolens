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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock

class MultiParserTest {
  @Test
  fun canParse_languageWithProvidedParser_returnsTrue() {
    val path = SourcePath("Test.mock")
    val parser1 = mock<Parser> { on { canParse(any()) } doReturn false }
    val parser2 = mock<Parser> { on { canParse(path) } doReturn true }

    val multiParser = MultiParser(listOf(parser1, parser2))

    assertTrue(multiParser.canParse(path))
  }

  @Test
  fun canParse_unknownLanguage_returnsFalse() {
    val parser1 = mock<Parser> { on { canParse(any()) } doReturn false }
    val parser2 = mock<Parser> { on { canParse(any()) } doReturn false }

    val multiParser = MultiParser(listOf(parser1, parser2))

    assertFalse(multiParser.canParse(SourcePath("Test.mock")))
  }

  @Test
  fun canParse_whenNoProvidedParsers_returnsFalse() {
    val multiParser = MultiParser(emptyList())

    assertFalse(multiParser.canParse(SourcePath("Test.mock")))
  }

  @Test
  fun tryParse_languageWithProvidedParser_returnsParsedSource() {
    val path = SourcePath("Test.mock")
    val rawSource = "{ 'invalid' : 2"
    val sourceFile = SourceFile(path)
    val parser1 =
      mock<Parser> {
        on { canParse(any()) } doReturn false
        on { parse(any(), any()) } doThrow (SyntaxErrorException("Unsupported!"))
      }
    val parser2 =
      mock<Parser> {
        on { canParse(path) } doReturn true
        on { parse(path, rawSource) } doReturn sourceFile
      }

    val multiParser = MultiParser(listOf(parser1, parser2))
    val result = multiParser.tryParse(path, rawSource)

    assertEquals(expected = ParseResult.Success(sourceFile), actual = result)
  }

  @Test
  fun tryParse_unknownLanguage_returnsSyntaxError() {
    val error = SyntaxErrorException("Unsupported!")
    val parser1 =
      mock<Parser> {
        on { canParse(any()) } doReturn false
        on { parse(any(), any()) } doThrow (error)
      }
    val parser2 =
      mock<Parser> {
        on { canParse(any()) } doReturn false
        on { parse(any(), any()) } doThrow (error)
      }

    val multiParser = MultiParser(listOf(parser1, parser2))
    val result = multiParser.tryParse(SourcePath("Test.mock"), "{ 'invalid' ")

    assertIs<ParseResult.SyntaxError>(result)
  }

  @Test
  fun tryParse_sourceWithErrors_returnsSyntaxError() {
    val error = SyntaxErrorException("Invalid source file!")
    val parser =
      mock<Parser> {
        on { canParse(any()) } doReturn true
        on { parse(any(), any()) } doThrow error
      }

    val multiParser = MultiParser(listOf(parser))
    val result = multiParser.tryParse(SourcePath("Test.mock"), "{ 'invalid': 2")

    assertEquals(expected = ParseResult.SyntaxError(error), actual = result)
  }

  @Test
  fun tryParse_whenNoProvidedParsers_returnsSyntaxError() {
    val multiParser = MultiParser(emptyList())
    val result = multiParser.tryParse(SourcePath("Test.mock"), "{ 'invalid' ")

    assertIs<ParseResult.SyntaxError>(result)
  }
}
