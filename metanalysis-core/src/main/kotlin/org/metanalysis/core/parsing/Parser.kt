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

import org.metanalysis.core.model.SourceNode
import java.util.ServiceLoader

/**
 * An abstract source file parser for a specific programming language.
 *
 * Parsers must have a public no-arg constructor and must supply an entry in the
 * `META-INF/services/org.metanalysis.core.parsing.Parser` configuration file.
 */
interface Parser {
    companion object {
        private val parsers = ServiceLoader.load(Parser::class.java)
        private val parsersByLanguage = parsers.associateBy(Parser::language)

        private val String.fileName: String
            get() = substringAfterLast(SourceNode.PATH_SEPARATOR)

        /**
         * Returns a parser which can interpret the given programming
         * `language`, or `null` if no such parser was provided.
         */
        fun getParserByLanguage(language: String): Parser? =
                parsersByLanguage[language]

        /**
         * Returns a parser which can interpret the given file `path`, or `null`
         * if no such parser was provided.
         */
        fun getParser(path: String): Parser? =
                parsers.firstOrNull { path.fileName.matches(it.pattern) }

        /**
         * Parses the given `sourceFile` and returns the result, or `null` if no
         * such parser was provided.
         */
        fun parse(sourceFile: SourceFile): Result? =
                getParser(sourceFile.path)?.parse(sourceFile)
    }

    /** The programming language which can be interpreted by this parser. */
    val language: String

    /** The pattern for file names which can be interpreted by this parser. */
    val pattern: Regex

    /**
     * Parses the given `sourceFile` and returns the result.
     *
     * @throws IllegalArgumentException if the file path doesn't match [pattern]
     */
    fun parse(sourceFile: SourceFile): Result
}
