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

import org.metanalysis.core.model.SourceUnit
import org.metanalysis.core.serialization.JsonException
import org.metanalysis.core.serialization.JsonModule

class ParserMock : Parser() {
    companion object {
        const val LANGUAGE: String = "Mock"
    }

    override val language: String = LANGUAGE

    override fun canParse(path: String): Boolean = path.endsWith(".mock")

    override fun parse(path: String, rawSource: String): SourceUnit = try {
        JsonModule.deserialize(rawSource.byteInputStream())
    } catch (e: JsonException) {
        throw SyntaxErrorException(e)
    }
}
