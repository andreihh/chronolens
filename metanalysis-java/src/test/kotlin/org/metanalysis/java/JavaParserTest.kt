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

package org.metanalysis.java

import org.metanalysis.core.model.SourceNode.SourceUnit
import org.metanalysis.core.parsing.Parser.Companion.getParserByLanguage
import org.metanalysis.core.parsing.Result
import org.metanalysis.core.parsing.SourceFile
import org.metanalysis.test.core.model.UnitBuilder
import org.metanalysis.test.core.model.sourceUnit
import kotlin.test.fail

abstract class JavaParserTest {
    private val defaultPath = "Test.java"
    private val parser = getParserByLanguage(JavaParser.LANGUAGE)
            ?: fail("Couldn't retrieve Java parser!")

    protected fun sourceUnit(init: UnitBuilder.() -> Unit): SourceUnit =
            sourceUnit(defaultPath, init)

    protected fun parse(
            source: String,
            path: String = defaultPath
    ): SourceUnit {
        val result = parser.parse(SourceFile(path, source))
        return when (result) {
            is Result.Success -> result.sourceUnit
            Result.SyntaxError -> fail()
        }
    }

    protected fun parseResult(
            source: String,
            path: String = defaultPath
    ): Result = parser.parse(SourceFile(path, source))
}
