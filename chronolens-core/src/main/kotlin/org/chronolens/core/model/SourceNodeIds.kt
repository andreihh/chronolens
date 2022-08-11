/*
 * Copyright 2021-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.model

import org.chronolens.core.model.SourceNodeId.Companion.SEPARATORS
import org.chronolens.core.model.SourcePath.Companion.PATH_SEPARATOR

/** The simple id of a [SourceNode]. */
public sealed interface SourceNodeId {
    abstract override fun toString(): String

    public companion object {
        /** Special characters that cannot occur in source node ids. */
        public const val SEPARATORS: String = "/:#\"\\\\"
    }
}

/**
 * The qualified path of a [SourceFile].
 *
 * @throws IllegalArgumentException if the given [path] is invalid
 */
public data class SourcePath(private val path: String) : SourceNodeId {
    init {
        require(isValid(path)) { "Invalid source path '$path'!" }
    }

    override fun toString(): String = path

    public companion object {
        /** Paths in [SourcePath] ids are separated by `/`. */
        public const val PATH_SEPARATOR: Char = '/'

        /** Returns whether the given [path] is valid. */
        @JvmStatic
        public fun isValid(path: String): Boolean =
            path.matches(sourcePathRegex) && "/$path/".indexOfAny(listOf("//", "/./", "/../")) == -1
    }
}

/**
 * The simple name of a [Type] or [Variable].
 *
 * @throws IllegalArgumentException if the given [identifier] is invalid
 */
public data class Identifier(private val identifier: String) : SourceNodeId {
    init {
        require(isValid(identifier)) { "Invalid identifier '$identifier'!" }
    }

    override fun toString(): String = identifier

    public companion object {
        /** Returns whether the given [identifier] is valid. */
        @JvmStatic
        public fun isValid(identifier: String): Boolean = identifier.matches(identifierRegex)
    }
}

/**
 * The signature of a [Function].
 *
 * @throws [IllegalArgumentException] if the given [signature] is invalid
 */
public data class Signature(private val signature: String) : SourceNodeId {
    init {
        require(isValid(signature)) { "Invalid signature '$signature'!" }
    }

    override fun toString(): String = signature

    public companion object {
        /** Returns whether the given [signature] is valid. */
        @JvmStatic
        public fun isValid(signature: String): Boolean = signature.matches(signatureRegex)
    }
}

private val fileComponentRegex = Regex("(?>[^$SEPARATORS]++)")
private val sourcePathRegex = Regex("$fileComponentRegex($PATH_SEPARATOR$fileComponentRegex)*+")
private val identifierRegex = Regex("(?>[^$SEPARATORS()]++)")
private val signatureRegex = Regex("(?>$identifierRegex\\([^$SEPARATORS]*\\))")
