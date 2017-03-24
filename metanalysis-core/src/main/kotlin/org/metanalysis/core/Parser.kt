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

package org.metanalysis.core

import java.io.File
import java.io.IOException
import java.net.URL

/** An abstract source file parser of a specific programming language. */
abstract class Parser {
    /** Indicates a syntax error detected in a source file. */
    class SyntaxError(
            message: String? = null,
            cause: Throwable? = null
    ) : Exception(message, cause)

    /**
     * Parses the given `source` code and returns the associated code metadata.
     *
     * @param source the source code which should be parsed
     * @return the source file metadata
     * @throws SyntaxError if the given `source` is not valid source code
     */
    @Throws(SyntaxError::class)
    abstract fun parse(source: String): SourceFile

    /**
     * Parses the given `file` and returns the associated code meta-data.
     *
     * @param file the file which should be parsed
     * @return the source file metadata
     * @throws SyntaxError if the given `file` contains invalid source code
     * @throws IOException if an error occurs trying to read the `file` content
     */
    @Throws(SyntaxError::class, IOException::class)
    fun parse(file: File): SourceFile = parse(file.readText())

    /**
     * Parses the content at the given `url` and returns the associated code
     * meta-data.
     *
     * @param url the location of the content which should be parsed
     * @return the source file metadata
     * @throws SyntaxError if the content at the specified `url` contains
     * invalid source code
     * @throws IOException if an error occurs trying to read the content at the
     * specified `url`
     */
    @Throws(SyntaxError::class)
    fun parse(url: URL): SourceFile = parse(url.readText())
}
