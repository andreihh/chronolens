/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.java

import kotlin.test.fail
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.parsing.Parser
import org.chronolens.core.parsing.Result
import org.chronolens.test.core.model.SourceFileBuilder
import org.chronolens.test.core.model.sourceFile

abstract class JavaParserTest {
    private val defaultPath = "Test.java"

    protected fun sourceFile(init: SourceFileBuilder.() -> Unit): SourceFile =
        sourceFile(defaultPath).build(init)

    protected fun parse(source: String, path: String = defaultPath): SourceFile =
        when (val result = Parser.parse(SourcePath(path), source)) {
            is Result.Success -> result.source
            else -> fail()
        }

    protected fun parseResult(source: String, path: String = defaultPath): Result =
        Parser.parse(SourcePath(path), source) ?: fail()
}
