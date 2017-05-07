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

package org.metanalysis.core.model

import java.io.File
import java.io.IOException
import java.util.ServiceConfigurationError
import java.util.ServiceLoader

/**
 * An abstract source file parser of a specific programming language.
 *
 * Parsers must have a public no-arg constructor.
 *
 * The file `META-INF/services/org.metanalysis.core.model.Parser` must be
 * provided and must contain the list of all provided parsers.
 */
abstract class Parser {
    companion object {
        private val parsers by lazy { ServiceLoader.load(Parser::class.java) }
        private val languageToParser by lazy {
            parsers.associateBy { parser -> parser.language.toLowerCase() }
        }
        private val extensionToParser by lazy {
            parsers.flatMap { parser ->
                parser.extensions.map { extension ->
                    extension.toLowerCase() to parser
                }
            }.toMap()
        }

        /**
         * Returns a parser which can interpret the given case-insensitive
         * programming `language`.
         *
         * @param language the language which must be supported by the parser
         * @return the parser which supports the given `language`, or `null` if
         * no such parser was provided
         * @throws ServiceConfigurationError if the configuration file couldn't
         * be loaded properly
         */
        @JvmStatic fun getByLanguage(language: String): Parser? =
                languageToParser[language.toLowerCase()]

        /**
         * Returns a parser which can interpret files with the given
         * case-insensitive `extension`.
         *
         * @param extension the file extension which must be supported by the
         * parser
         * @return the parser which supports the given file `extension`, or
         * `null` if no such parser was provided
         */
        @JvmStatic fun getByExtension(extension: String): Parser? =
                extensionToParser[extension.toLowerCase()]

        /**
         * Parses the given `file` and returns the associated code meta-data.
         *
         * @param file the file which should be parsed
         * @return the source file metadata
         * @throws SyntaxError if the `file` contains invalid source code
         * @throws UnsupportedExtensionException if none of the provided parsers
         * cant interpret the given `file` extensions
         * @throws IOException if any input related errors occur
         */
        @Throws(IOException::class)
        @JvmStatic fun parse(file: File): SourceFile =
                getByExtension(file.extension)?.parse(file.readText())
                        ?: throw UnsupportedExtensionException(file.extension)
    }

    /** Indicates that a parsed file contains invalid code. */
    class SyntaxError : IOException {
        constructor(message: String) : super(message)
        constructor(cause: Throwable) : super(cause)
    }

    class UnsupportedExtensionException(extension: String) : IOException(
            "Can't interpret extension '$extension'!"
    )

    /** The programming language which can be interpreted by this parser. */
    protected abstract val language: String

    /** The file extensions which can be interpreted by this parser. */
    protected abstract val extensions: Set<String>

    /**
     * Parses the given `source` code and returns the associated code metadata.
     *
     * @param source the source code which should be parsed
     * @return the source file metadata
     * @throws SyntaxError if the given `source` is not valid source code
     */
    @Throws(SyntaxError::class)
    abstract fun parse(source: String): SourceFile
}
