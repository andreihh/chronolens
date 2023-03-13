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

import java.io.ByteArrayOutputStream
import org.chronolens.api.parsing.Parser
import org.chronolens.api.parsing.SyntaxErrorException
import org.chronolens.api.serialization.SerializationException
import org.chronolens.api.serialization.deserialize
import org.chronolens.core.serialization.JsonModule
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath

public class FakeParser : Parser {
  override fun canParse(path: SourcePath): Boolean = path.toString().endsWith(".fake")

  @Throws(SyntaxErrorException::class)
  override fun parse(path: SourcePath, rawSource: String): SourceFile {
    if (!canParse(path)) {
      throw SyntaxErrorException("Cannot parse '$path'!")
    }
    try {
      val sourceFile = JsonModule.deserialize<SourceFile>(rawSource.byteInputStream())
      check(sourceFile.path == path) { "Parsed invalid path '${sourceFile.path}'" }
      return sourceFile
    } catch (e: SerializationException) {
      throw SyntaxErrorException(e)
    }
  }

  @Throws(SyntaxErrorException::class)
  public fun unparse(sourceFile: SourceFile): String {
    if (!canParse(sourceFile.path)) {
      throw SyntaxErrorException("Cannot unparse '${sourceFile.path}'!")
    }
    return try {
      val out = ByteArrayOutputStream()
      out.use { JsonModule.serialize(out, sourceFile) }
      out.toString()
    } catch (e: SerializationException) {
      throw SyntaxErrorException(e)
    }
  }
}
