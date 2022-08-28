/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.core.parsing

import java.io.ByteArrayOutputStream
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.parsing.Parser
import org.chronolens.core.parsing.SyntaxErrorException
import org.chronolens.core.serialization.JsonException
import org.chronolens.core.serialization.JsonModule

public class FakeParser : Parser {
    override fun canParse(path: SourcePath): Boolean = path.toString().endsWith(".fake")

    @Throws(SyntaxErrorException::class)
    override fun parse(path: SourcePath, rawSource: String): SourceFile {
        if (!canParse(path)) {
            throw SyntaxErrorException("Cannot parse '$path'!")
        }
        return try {
            JsonModule.deserialize(rawSource.byteInputStream())
        } catch (e: JsonException) {
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
        } catch (e: JsonException) {
            throw SyntaxErrorException(e)
        }
    }
}
