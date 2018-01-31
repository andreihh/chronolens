/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.metanalysis.core.parsing

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.fail

class ParserTest {
    @Test fun `test get unprovided parser by language returns null`() {
        assertNull(Parser.getParserByLanguage("Java"))
    }

    @Test fun `test get unprovided parser returns null`() {
        assertNull(Parser.getParser("Test.java"))
    }

    @Test fun `test get parser for unknown language returns null`() {
        assertNull(Parser.getParser("mp3"))
    }

    @Test fun `test parse source with errors throws`() {
        val sourceFile =
            SourceFile(path = "res.mock", rawContent = "{ \"invalid\":2")
        val result = Parser.parse(sourceFile)
        assertEquals(Result.SyntaxError, result )
    }

    @Test fun `test parse invalid path throws`() {
        val parser = Parser.getParserByLanguage(ParserMock.LANGUAGE)
            ?: fail("Parser not provided!")
        val sourceFile = SourceFile(path = "res.moc", rawContent = "{}")
        assertFailsWith<IllegalArgumentException> {
            parser.parse(sourceFile)
        }
    }
}
