/*
 * Copyright 2017-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
    /** Returns the simple, unqualified id of this source node. */
    public val simpleId: String
        get() =
            when (this) {
                is SourceFile -> path
                is Type -> name
                is Function -> signature
                is Variable -> name
            }

    /** The kind of this source node. Denotes a final, non-abstract type. */
    public val kind: SourceNodeKind
        get() =
            when (this) {
                is SourceFile -> SOURCE_FILE
                is Type -> TYPE
                is Function -> FUNCTION
                is Variable -> VARIABLE
            }

    /** The child source nodes contained in this source node. */
    public val children: Collection<SourceEntity>
        get() =
            when (this) {
                is SourceFile -> entities
                is Type -> members
                else -> emptySet()
            }
}

/** An abstract representation of a source entity within a [SourceFile]. */
public sealed class SourceEntity : SourceNode()

/**
 * The source code metadata of a source file.
 *
 * @property path the fully qualified path of this file
 * @property entities the source entities contained by this source file
 * @throws IllegalArgumentException if the [path] is not valid or if the ids of the [entities] are
 * not valid or if the [entities] contain duplicated ids
 */
public data class SourceFile(
    val path: String,
    val entities: Set<SourceEntity> = emptySet(),
) : SourceNode() {

    init {
        validatePath(path)
        validateChildrenIds()
    }
}

/**
 * A type declaration found inside a [SourceFile].
 *
 * @property name the name of this type
 * @property supertypes the supertypes of this type
 * @property modifiers the modifiers of this type
 * @property members the members of this type (functions, variables and contained types)
 * @throws IllegalArgumentException if the [name] is not valid or if the ids of the [members] are
 * not valid or if the [members] contain duplicated ids
 */
public data class Type(
    val name: String,
    val supertypes: Set<String> = emptySet(),
    val modifiers: Set<String> = emptySet(),
    val members: Set<SourceEntity> = emptySet(),
) : SourceEntity() {

    init {
        validateIdentifier(name)
        validateChildrenIds()
    }
}

/**
 * A function declaration found inside a [SourceFile].
 *
 * The parameters of a function must have unique names.
 *
 * @property signature the signature of this function; should be `name(type_1, type_2, ...)` if
 * function overloading at the type level is allowed, or `name(n)` (where `n` is the arity of the
 * function) if function overloading at the arity level is allowed, or `name()` otherwise.
 * @property parameters the names of the parameters of this function
 * @property modifiers the modifiers of this function
 * @property body the body lines of this function, or an empty list if it doesn't have a body
 * @throws IllegalArgumentException if the [signature] is not valid or if the [parameters] contain
 * invalid or duplicated names
 */
public data class Function(
    val signature: String,
    val parameters: List<String> = emptyList(),
    val modifiers: Set<String> = emptySet(),
    val body: List<String> = emptyList(),
) : SourceEntity() {

    init {
        validateSignature(signature)
        validateParameterNames()
    }
}

/**
 * A variable declaration found inside a [SourceFile].
 *
 * @property name the name of this variable
 * @property modifiers the modifiers of this variable
 * @property initializer the initializer lines of this variable, or an empty list if it doesn't have
 * an initializer
 * @throws IllegalArgumentException if the [name] is not valid
 */
public data class Variable(
    val name: String,
    val modifiers: Set<String> = emptySet(),
    val initializer: List<String> = emptyList(),
) : SourceEntity() {

    init {
        validateIdentifier(name)
    }
}

/** The final, non-abstract type of a source node. */
public enum class SourceNodeKind {
    SOURCE_FILE,
    TYPE,
    FUNCTION,
    VARIABLE
}
