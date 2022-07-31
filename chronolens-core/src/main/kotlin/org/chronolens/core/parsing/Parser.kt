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

/**
 * An abstract source file parser for a specific programming language.
 *
 * Parsers must have a public no-arg constructor and must supply an entry in the
 * `META-INF/services/org.chronolens.core.parsing.Parser` configuration file.
 */
public abstract class Parser {
    /** Returns whether this parser can interpret the given file [path]. */
    protected abstract fun canParse(path: String): Boolean

    /**
     * Parses the given `UTF-8` encoded [rawSource] assuming it is located at the specified [path].
     *
     * @throws SyntaxErrorException if the [rawSource] contains errors
     * @throws IllegalArgumentException if the given [path] is not a valid [SourceFile] path
     */
    @Throws(SyntaxErrorException::class)
    protected abstract fun parse(path: String, rawSource: String): SourceFile

    public companion object {
        private val parsers = ServiceLoader.load(Parser::class.java)

        /**
         * Returns a parser which can interpret the given file [path], or `null` if no such parser
         * was provided.
         */
        private fun getParser(path: String): Parser? = parsers.firstOrNull { it.canParse(path) }

        /** Returns whether a provided parser can interpret the given file [path]. */
        public fun canParse(path: String): Boolean = getParser(path) != null

        /**
         * Parses the given `UTF-8` encoded [rawSource] assuming it is located at the specified
         * [path] and returns the result, or `null` if no such parser was provided.
         *
         * @throws IllegalArgumentException if the given [path] is not a valid [SourceFile] path
         * @throws IllegalStateException if the parsed source file has a different path than the
         * given [path]
         */
        public fun parse(path: String, rawSource: String): Result? {
            val parser = getParser(path) ?: return null
            return try {
                val source = parser.parse(path, rawSource)
                check(source.path.path == path) {
                    "Source '${source.path}' must be located at '$path'!"
                }
                Result.Success(source)
            } catch (e: SyntaxErrorException) {
                Result.SyntaxError
            }
        }
    }
}
