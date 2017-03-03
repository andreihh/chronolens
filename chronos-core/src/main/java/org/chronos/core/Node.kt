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

package org.chronos.core

import kotlin.reflect.KClass

/** Abstract metadata of a declaration within a source file. */
sealed class Node {
    /**
     *  A unique identifier other nodes of the same type.
     */
    abstract val identifier: String

    /**
     * Two nodes are equal if and only if they have the same [identifier] and
     * belong to the same node subtype.
     */
    final override fun equals(other: Any?): Boolean =
            other is Node && identifier == other.identifier
                    && javaClass == other.javaClass

    final override fun hashCode(): Int = identifier.hashCode()

    override abstract fun toString(): String

    /**
     * A type declared within a source file.
     *
     * The [identifier] of a type is its [name].
     *
     * @property name the name of this type
     * @property supertypes the supertypes of this type
     * @property members the members of this type (variables, functions and
     * contained types)
     */
    data class Type(
            val name: String,
            val supertypes: Set<String> = emptySet(),
            val members: Set<Node> = emptySet()
    ) : Node() {
        override val identifier: String
            get() = name

        @Transient private val memberMap =
                members.associateBy { it::class to it.identifier }

        fun find(type: KClass<out Node>, identifier: String): Node? =
                memberMap[type to identifier]

        inline fun <reified T : Node> find(identifier: String): T? =
                find(T::class, identifier) as T?
    }

    /**
     * A variable declared within a source file.
     *
     * The [identifier] of a variable is its [name].
     *
     * @property name the name of this variable
     * @property initializer the canonical string representation of the
     * initializer of this variable, or `null` if it doesn't have an initializer
     */
    data class Variable(
            val name: String,
            val initializer: String? = null
    ) : Node() {
        override val identifier: String
            get() = name
    }

    /**
     * A function declared within a source file.
     *
     * The parameters of a function must have unique names.
     *
     * The [identifier] of a function is its [signature].
     *
     * The signature of a function should be `name(type_1, type_2, ...)` if
     * function overloading at the type level is allowed, or `name(n)` (where
     * `n` is the arity of the function) if function overloading at the arity
     * level is allowed, or `name` otherwise.
     *
     * @property signature the signature of this function
     * @property parameters the parameters of this function
     * @property body the canonical string representation of the body of this
     * function, or `null` if it doesn't have a body
     * @throws IllegalArgumentException if multiple `parameters` have the same
     * `name`
     */
    data class Function(
            val signature: String,
            val parameters: List<Variable>,
            val body: String? = null
    ) : Node() {
        override val identifier: String
            get() = signature

        init {
            require(parameters.distinctBy { it.name }.size == parameters.size) {
                "$parameters contains duplicated parameters!"
            }
        }
    }
}
