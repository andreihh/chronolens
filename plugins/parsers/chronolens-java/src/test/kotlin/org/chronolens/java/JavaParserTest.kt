/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import org.chronolens.api.parsing.ParseResult
import org.chronolens.api.parsing.Parser
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.chronolens.test.model.SourceFileBuilder
import org.chronolens.test.model.sourceFile

abstract class JavaParserTest {
  private val defaultPath = "Test.java"

  protected fun sourceFile(init: SourceFileBuilder.() -> Unit): SourceFile =
    sourceFile(defaultPath, init)

  protected fun parse(source: String, path: String = defaultPath): SourceFile =
    when (val result = Parser.tryParse(SourcePath(path), source)) {
      is ParseResult.Success -> result.source
      else -> fail()
    }

  protected fun parseResult(source: String, path: String = defaultPath): ParseResult {
    val sourcePath = SourcePath(path)
    if (!Parser.canParse(sourcePath)) {
      fail()
    }
    return Parser.tryParse(sourcePath, source)
  }
}
