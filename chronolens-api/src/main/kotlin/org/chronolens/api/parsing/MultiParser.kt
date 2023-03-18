/*
 * Copyright 2022-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath

/**
 * A [Parser] capable of parsing multiple languages by delegating to one of the provided [parsers].
 */
internal class MultiParser(private val parsers: Iterable<Parser>) : Parser {
  override fun canParse(path: SourcePath): Boolean = parsers.any { it.canParse(path) }

  override fun parse(path: SourcePath, rawSource: String): SourceFile {
    val parser =
      parsers.find { it.canParse(path) }
        ?: throw SyntaxErrorException("No parser found for source file '$path'!")
    return parser.parse(path, rawSource)
  }
}
