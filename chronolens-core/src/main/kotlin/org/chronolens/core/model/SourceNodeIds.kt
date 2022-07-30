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

/** The simple id of a [SourceNode]. */
public sealed class SourceNodeId {
    abstract override fun toString(): String
}

/** The fully qualified path of a [SourceFile]. */
public data class SourcePath(val path: String) : SourceNodeId() {
    init {
        validatePath(path)
    }

    override fun toString(): String = path

    public companion object {
        /** Paths in [SourcePath] ids are separated by `/`. */
        public const val PATH_SEPARATOR: Char = '/'
    }
}

/** The simple name of a [Type] or [Variable]. */
public data class Identifier(val identifier: String) : SourceNodeId() {
    init {
        validateIdentifier(identifier)
    }

    override fun toString(): String = identifier
}

/** The signature of a [Function]. */
public data class Signature(val signature: String) : SourceNodeId() {
    init {
        validateSignature(signature)
    }

    override fun toString(): String = signature
}
