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

/**
 * An immutable abstract representation of a source node.
 *
 * All collections passed as constructor parameters must be unmodifiable.
 */
sealed class SourceNode {
    /**
     * A fully qualified identifier among all nodes inside a [Project].
     *
     * The fully qualified identifier of a nested node will be the fully
     * qualified identifier of the enclosing node followed by the corresponding
     * separator and then followed by the simple identifier of the node. The
     * simple identifiers must not contain `/`, `:`, `'`, `"` or `\`.
     */
    abstract val id: String

    companion object {
        /** Paths in [SourceUnit] ids are separated by `/`. */
        const val PATH_SEPARATOR: Char = '/'

        /** Identifiers in [SourceEntity] ids are separated by `:`. */
        const val ENTITY_SEPARATOR: Char = ':'
    }
}

/** An abstract representation of a source entity within a [SourceUnit]. */
sealed class SourceEntity : SourceNode()

/**
 * The source code metadata of a source file.
 *
 * @property id the fully qualified identifier of this unit
 * @property entities the source entities contained by this source file
 * @throws IllegalArgumentException if the [id] is not valid or if the ids of
 * the [entities] are not valid or if the [entities] contain duplicated ids
 */
data class SourceUnit(
    override val id: String,
    val entities: Collection<SourceEntity> = emptyList()
) : SourceNode() {

    init {
        validateUnitId(id)
        validateChildrenIds()
    }

    /** The fully qualified path of this unit, equal to [id]. */
    val path: String get() = id
}

/**
 * A type declaration found inside a [SourceUnit].
 *
 * @property id the fully qualified name of this type
 * @property modifiers the modifiers of this type
 * @property supertypes the supertypes of this type
 * @property members the members of this type (functions, variables and
 * contained types)
 * @throws IllegalArgumentException if the [id] is not valid or if the ids of
 * the [members] are not valid or if the [members] contain duplicated ids
 */
data class Type(
    override val id: String,
    val modifiers: Set<String> = emptySet(),
    val supertypes: Set<String> = emptySet(),
    val members: Collection<SourceEntity> = emptyList()
) : SourceEntity() {

    init {
        validateTypeId(id)
        validateChildrenIds()
    }

    /** The simple name of this type. */
    val name: String get() = id.substringAfterLast(ENTITY_SEPARATOR)
}

/**
 * A function declaration found inside a [SourceUnit].
 *
 * The parameters of a function must have unique names.
 *
 * @property id the fully qualified signature of this function
 * @property modifiers the modifiers of this function
 * @property parameters the parameters of this function
 * @property body the body lines of this function, or an empty list if it
 * doesn't have a body
 * @throws IllegalArgumentException if the [id] is not valid or if the ids of
 * the [parameters] are not valid or if the [parameters] contain duplicated ids
 */
data class Function(
    override val id: String,
    val modifiers: Set<String> = emptySet(),
    val parameters: List<Variable> = emptyList(),
    val body: List<String> = emptyList()
) : SourceEntity() {

    init {
        validateFunctionId(id)
        validateChildrenIds()
    }

    /**
     * The simple signature of this function.
     *
     * The signature of a function should be `name(type_1, type_2, ...)` if
     * function overloading at the type level is allowed, or `name(n)` (where
     * `n` is the arity of the function) if function overloading at the arity
     * level is allowed, or `name()` otherwise.
     */
    val signature: String get() = id.substringAfterLast(ENTITY_SEPARATOR)
}

/**
 * A variable declaration found inside a [SourceUnit].
 *
 * @property id the fully qualified name of this variable
 * @property modifiers the modifiers of this variable
 * @property initializer the initializer lines of this variable, or an empty
 * list if it doesn't have an initializer
 * @throws IllegalArgumentException if the [id] is not valid
 */
data class Variable(
    override val id: String,
    val modifiers: Set<String> = emptySet(),
    val initializer: List<String> = emptyList()
) : SourceEntity() {

    init {
        validateVariableId(id)
    }

    /** The simple name of this variable. */
    val name: String get() = id.substringAfterLast(ENTITY_SEPARATOR)
}
