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

import org.metanalysis.core.model.SourceFile

import java.io.File
import java.io.IOException
import java.util.ServiceLoader

/**
 * An abstract source file parser for a specific programming language.
 *
 * Parsers must have a public no-arg constructor.
 *
 * The file `META-INF/services/org.metanalysis.core.parsing.Parser` must be
 * provided and must contain the list of all parser implementations.
 */
abstract class Parser {
    companion object {
        private val parsers = ServiceLoader.load(Parser::class.java)
        private val languageToParser =
            parsers.associateBy { parser -> parser.language.toLowerCase() }
        private val extensionToParser =
            parsers.flatMap { parser ->
                parser.extensions.map { extension ->
                    extension.toLowerCase() to parser
                }
            }.toMap()

        /**
         * Returns a parser which can interpret the given programming language.
         *
         * @param language the case-insensitive language which must be supported
         * by the parser
         * @return the parser which supports the given `language`, or `null` if
         * no such parser was provided
         */
        @JvmStatic fun getByLanguage(language: String): Parser? =
                languageToParser[language.toLowerCase()]

        /**
         * Returns a parser which can interpret files with the given extension.
         *
         * @param extension the case-insensitive file extension which must be
         * supported by the parser
         * @return the parser which supports the given file `extension`, or
         * `null` if no such parser was provided
         */
        @JvmStatic fun getByExtension(extension: String): Parser? =
                extensionToParser[extension.toLowerCase()]

        /**
         * Parses the given file and returns the associated code metadata.
         *
         * @param file the file which should be parsed
         * @return the source file metadata, or `null` if none of the provided
         * parsers can interpret the given `file` extension
         * @throws SyntaxErrorException if the given `file` contains invalid
         * code
         * @throws IOException if any input related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic fun parse(file: File): SourceFile? =
                getByExtension(file.extension)?.parse(file.readText())
    }

    /** The programming language which can be interpreted by this parser. */
    protected abstract val language: String

    /** The file extensions which can be interpreted by this parser. */
    protected abstract val extensions: Set<String>

    /**
     * Parses the given `UTF-8` encoded `source` code and returns the associated
     * code metadata.
     *
     * @throws SyntaxErrorException if the given `source` contains invalid code
     */
    @Throws(SyntaxErrorException::class)
    abstract fun parse(source: String): SourceFile
}