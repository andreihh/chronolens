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

package org.metanalysis.core.model

import org.junit.Test

import org.metanalysis.core.model.Parser.Companion.getByExtension
import org.metanalysis.core.model.Parser.Companion.getByLanguage
import org.metanalysis.core.model.Parser.SyntaxError
import org.metanalysis.test.core.model.ParserMock
import org.metanalysis.test.core.model.assertEquals

import java.io.File
import java.io.IOException

import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParserTest {
    @Test fun `test get provided parser by language returns non-null`() {
        assertTrue(getByLanguage(ParserMock.LANGUAGE) is ParserMock)
    }

    @Test fun `test get provided parser by extension returns non-null`() {
        ParserMock.EXTENSIONS.forEach {
            assertTrue(getByExtension(it) is ParserMock)
        }
    }

    @Test fun `test get unprovided parser by language returns null`() {
        assertNull(getByLanguage("Java"))
    }

    @Test fun `test get unprovided parser by extension returns null`() {
        assertNull(getByExtension("java"))
    }

    @Test fun `test parse file`() {
        val file = File("src/test/resources/resource.mock")
        val expected = SourceFile()
        val actual = Parser.parse(file)
        assertEquals(expected, actual)
    }

    @Test fun `test parse file of unknown extension returns null`() {
        val parser = Parser::class.qualifiedName
        val file = File("src/test/resources/META-INF/services/$parser")
        assertNull(Parser.parse(file))
    }

    @Test fun `test parse non-existent file throws`() {
        assertFailsWith<IOException> {
            Parser.parse(File("non-existing-file.mock"))
        }
    }

    @Test fun `test parse source with errors throws`() {
        val parser = requireNotNull(Parser.getByLanguage(ParserMock.LANGUAGE))
        assertFailsWith<SyntaxError> {
            parser.parse("{ \"invalid property\":2")
        }
    }
}
