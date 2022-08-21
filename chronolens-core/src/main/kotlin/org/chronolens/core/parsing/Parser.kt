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

package org.chronolens.core.parsing

import java.util.ServiceLoader
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath

/**
 * An abstract source file parser for a specific programming language.
 *
 * Parsers must have a public no-arg constructor and must supply an entry in the
 * `META-INF/services/org.chronolens.core.parsing.Parser` configuration file.
 */
public interface Parser {
    /** Returns whether this parser can interpret the given file [path]. */
    public fun canParse(path: SourcePath): Boolean

    /**
     * Parses the given `UTF-8` encoded [rawSource] assuming it is located at the specified [path].
     *
     * @throws SyntaxErrorException if the [rawSource] contains errors or cannot be parsed
     * @throws IllegalArgumentException if the parsed source file has a different path than the
     * given [path]
     */
    @Throws(SyntaxErrorException::class)
    public fun parse(path: SourcePath, rawSource: String): SourceFile

    /**
     * Parses the given `UTF-8` encoded [rawSource] assuming it is located at the specified [path]
     * and returns the [Result].
     *
     * @throws IllegalArgumentException if the parsed source file has a different path than the
     * given [path]
     */
    public fun tryParse(path: SourcePath, rawSource: String): Result =
        try {
            val source = parse(path, rawSource)
            require(source.path == path) { "Source '${source.path}' must be located at '$path'!" }
            Result.Success(source)
        } catch (e: SyntaxErrorException) {
            Result.SyntaxError
        }

    public companion object Registry :
        Parser by MultiParser(ServiceLoader.load(Parser::class.java).toList())
}
