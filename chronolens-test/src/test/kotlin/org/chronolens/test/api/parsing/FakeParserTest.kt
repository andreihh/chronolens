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

package org.chronolens.test.api.parsing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.chronolens.api.parsing.ParseResult
import org.chronolens.api.parsing.SyntaxErrorException
import org.chronolens.model.SourcePath
import org.chronolens.test.model.function
import org.chronolens.test.model.sourceFile
import org.chronolens.test.model.type
import org.chronolens.test.model.variable

class FakeParserTest {
  private val parser = FakeParser()

  @Test
  fun canParse_languageWithProvidedParser_returnsTrue() {
    assertTrue(parser.canParse(SourcePath("Test.kts")))
  }

  @Test
  fun canParse_unknownLanguage_returnsFalse() {
    assertFalse(parser.canParse(SourcePath("file.mp3")))
  }

  @Test
  fun tryParse_sourceWithErrors_returnsSyntaxError() {
    val path = SourcePath("res.kts")
    val rawSource = "sourceFile(\"res.kts\") {"

    assertIs<ParseResult.SyntaxError>(parser.tryParse(path, rawSource))
  }

  @Test
  fun tryParse_sourceWithUnsupportedPath_returnsSyntaxError() {
    val path = SourcePath("res.mock")
    val rawSource = "sourceFile(\"res.mock\") {}"

    assertIs<ParseResult.SyntaxError>(parser.tryParse(path, rawSource))
  }

  @Test
  fun tryParse_sourceWithDifferentPathThrows() {
    val path = SourcePath("Test.kts")
    val rawSource = "sourceFile(\"Main.kts\") {}"

    assertFailsWith<IllegalStateException> { parser.tryParse(path, rawSource) }
  }

  @Test
  fun tryParse_unparsedSourceFile_returnsSameSourceFile() {
    val expectedSourceFile =
      sourceFile("Main.kts") {
        +type("Main") {
          supertypes("Object")
          modifiers("public", "class")
          +function("main(String[])") {
            parameters("args")
            modifiers("public", "static", "void")
            body("{ return 0; }")
          }
        }
        +variable("VERSION") {
          modifiers("public", "static", "final", "int")
          initializer("5;")
        }
      }

    val actualSourceFile = parser.parse(expectedSourceFile.path, parser.unparse(expectedSourceFile))

    assertEquals(expected = expectedSourceFile, actual = actualSourceFile)
  }

  @Test
  fun unparse_unknownLanguage_throws() {
    assertFailsWith<SyntaxErrorException> { parser.unparse(sourceFile("Main.mock") {}) }
  }

  @Test
  fun unparse_omitsEmptyFields() {
    val sourceFile =
      sourceFile("Main.kts") {
        +type("Main") { +function("main(String[])") {} }
        +type("Test") {}
        +variable("VERSION") {}
      }
    val expectedRawSource =
      """
      sourceFile("Main.kts") {
        +type("Main") {
          +function("main(String[])") {
          }
        }
        +type("Test") {
        }
        +variable("VERSION") {
        }
      }"""
        .trimIndent()

    val actualRawSource = parser.unparse(sourceFile)

    println("Expected source:")
    println(expectedRawSource)

    println("Actual source:")
    println(actualRawSource)

    assertEquals(expected = expectedRawSource, actual = actualRawSource)
  }
}
