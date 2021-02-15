/*
 * Copyright 2017-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.SourceNodeKind.FUNCTION
import org.chronolens.core.model.SourceNodeKind.SOURCE_FILE
import org.chronolens.core.model.SourceNodeKind.TYPE
import org.chronolens.core.model.SourceNodeKind.VARIABLE

/**
 * An immutable abstract representation of a source node.
 *
 * All collections passed as constructor parameters must be unmodifiable.
 */
public sealed class SourceNode {
    /**
     * A fully qualified identifier among all nodes inside a [SourceTree].
     *
     * The fully qualified identifier of a nested node will be the fully
     * qualified identifier of the enclosing node followed by the corresponding
     * separator and then followed by the simple identifier of the node. The
     * simple identifiers must not contain `/`, `:`, `'`, `"` or `\`.
     */
    public abstract val id: String

    /** Returns the simple, unqualified id of this source node. */
    public val simpleId: String get() = when (this) {
        is SourceFile -> path
        is Type -> name
        is Function -> signature
        is Variable -> name
    }

    /** This kind of this source node. Denotes a final, non-abstract type. */
    public val kind: SourceNodeKind get() = when (this) {
        is SourceFile -> SOURCE_FILE
        is Type -> TYPE
        is Function -> FUNCTION
        is Variable -> VARIABLE
    }

    /** The child source nodes contained in this source node. */
    public val children: Collection<SourceEntity>
        get() = when (this) {
            is SourceFile -> entities
            is Type -> members
            else -> emptySet()
        }

    public companion object {
        /** Paths in [SourceFile] ids are separated by `/`. */
        public const val PATH_SEPARATOR: Char = '/'

        /** [Type] identifiers are separated by `:` from the parent id. */
        public const val CONTAINER_SEPARATOR: Char = ':'

        /**
         * [Function] and [Variable] identifiers are separated by `#` from the
         * parent id.
         */
        public const val MEMBER_SEPARATOR: Char = '#'
    }
}

/** An abstract representation of a source entity within a [SourceFile]. */
public sealed class SourceEntity : SourceNode()

/**
 * The source code metadata of a source file.
 *
 * @property id the fully qualified identifier of this file
 * @property entities the source entities contained by this source file
 * @throws IllegalArgumentException if the [id] is not valid or if the ids of
 * the [entities] are not valid or if the [entities] contain duplicated ids
 */
public data class SourceFile(
    override val id: String,
    val entities: Set<SourceEntity> = emptySet(),
) : SourceNode() {

    init {
        validateFileId(id)
        validateChildrenIds()
    }

    /** The fully qualified path of this file, equal to [id]. */
    public val path: String get() = id
}

/**
 * A type declaration found inside a [SourceFile].
 *
 * @property id the fully qualified name of this type
 * @property supertypes the supertypes of this type
 * @property modifiers the modifiers of this type
 * @property members the members of this type (functions, variables and
 * contained types)
 * @throws IllegalArgumentException if the [id] is not valid or if the ids of
 * the [members] are not valid or if the [members] contain duplicated ids
 */
public data class Type(
    override val id: String,
    val supertypes: Set<String> = emptySet(),
    val modifiers: Set<String> = emptySet(),
    val members: Set<SourceEntity> = emptySet(),
) : SourceEntity() {

    init {
        validateTypeId(id)
        validateChildrenIds()
    }

    /** The simple name of this type. */
    public val name: String get() = id.substringAfterLast(CONTAINER_SEPARATOR)
}

/**
 * A function declaration found inside a [SourceFile].
 *
 * The parameters of a function must have unique names.
 *
 * @property id the fully qualified signature of this function
 * @property parameters the names of the parameters of this function
 * @property modifiers the modifiers of this function
 * @property body the body lines of this function, or an empty list if it
 * doesn't have a body
 * @throws IllegalArgumentException if the [id] is not valid or if the ids of
 * the [parameters] are not valid or if the [parameters] contain duplicated ids
 */
public data class Function(
    override val id: String,
    val parameters: List<String> = emptyList(),
    val modifiers: Set<String> = emptySet(),
    val body: List<String> = emptyList(),
) : SourceEntity() {

    init {
        validateFunctionId(id)
        validateParameterNames()
    }

    /**
     * The simple signature of this function.
     *
     * The signature of a function should be `name(type_1, type_2, ...)` if
     * function overloading at the type level is allowed, or `name(n)` (where
     * `n` is the arity of the function) if function overloading at the arity
     * level is allowed, or `name()` otherwise.
     */
    public val signature: String get() = id.substringAfterLast(MEMBER_SEPARATOR)
}

/**
 * A variable declaration found inside a [SourceFile].
 *
 * @property id the fully qualified name of this variable
 * @property modifiers the modifiers of this variable
 * @property initializer the initializer lines of this variable, or an empty
 * list if it doesn't have an initializer
 * @throws IllegalArgumentException if the [id] is not valid
 */
public data class Variable(
    override val id: String,
    val modifiers: Set<String> = emptySet(),
    val initializer: List<String> = emptyList(),
) : SourceEntity() {

    init {
        validateVariableId(id)
    }

    /** The simple name of this variable. */
    public val name: String get() = id.substringAfterLast(MEMBER_SEPARATOR)
}

/** The final, non-abstract type of a source node. */
public enum class SourceNodeKind {
    SOURCE_FILE, TYPE, FUNCTION, VARIABLE
}
