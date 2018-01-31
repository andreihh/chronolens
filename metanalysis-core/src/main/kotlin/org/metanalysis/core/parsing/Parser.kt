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
import java.util.ServiceLoader

/**
 * An abstract source file parser for a specific programming language.
 *
 * Parsers must have a public no-arg constructor and must supply an entry in the
 * `META-INF/services/org.metanalysis.core.parsing.Parser` configuration file.
 */
abstract class Parser {
    /** The programming language which can be interpreted by this parser. */
    protected abstract val language: String

    /** Returns whether this parser can interpret the given file [path]. */
    protected abstract fun canParse(path: String): Boolean

    /**
     * Parses the given [rawSource] assuming it is located at the given [path].
     *
     * @throws SyntaxErrorException if the [rawSource] contains errors
     */
    @Throws(SyntaxErrorException::class)
    protected abstract fun parse(path: String, rawSource: String): SourceUnit

    /**
     * Parses the given [sourceFile] and returns the result.
     *
     * @throws IllegalArgumentException if the file can't be interpreted
     */
    fun parse(sourceFile: SourceFile): Result = try {
        val (path, rawSource) = sourceFile
        require(canParse(path)) { "Can't interpreted the given file '$path'!" }
        val unit = parse(path, rawSource)
        check(unit.path == path)
        Result.Success(unit)
    } catch (e: SyntaxErrorException) {
        Result.SyntaxError
    }

    companion object {
        private val parsers = ServiceLoader.load(Parser::class.java)
        private val parsersByLanguage = parsers.associateBy(Parser::language)

        /**
         * Returns a parser which can interpret the given programming
         * [language], or `null` if no such parser was provided.
         */
        fun getParserByLanguage(language: String): Parser? =
            parsersByLanguage[language]

        /**
         * Returns a parser which can interpret the given file [path], or `null`
         * if no such parser was provided.
         */
        fun getParser(path: String): Parser? =
            parsers.firstOrNull { it.canParse(path) }

        /**
         * Parses the given [sourceFile] and returns the result, or `null` if no
         * such parser was provided.
         */
        fun parse(sourceFile: SourceFile): Result? =
            getParser(sourceFile.path)?.parse(sourceFile)
    }
}
