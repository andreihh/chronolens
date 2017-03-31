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

import kotlin.reflect.KClass

/** Abstract metadata of a declaration within a source file. */
sealed class Node {
    /** A unique identifier among other nodes of the same type. */
    abstract val identifier: String

    /** The properties of this node. */
    abstract val properties: Map<String, String>

    /**
     * Two nodes are equal if and only if they have the same [identifier] and
     * belong to the same node subtype.
     */
    final override fun equals(other: Any?): Boolean = other is Node
            && javaClass == other.javaClass
            && identifier == other.identifier

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
            val members: Set<Node> = emptySet(),
            override val properties: Map<String, String> = emptyMap()
    ) : Node() {
        override val identifier: String
            get() = name

        @Transient private val memberMap =
                members.associateBy { it::class to it.identifier }

        /**
         * Returns the member with the specified `nodeType` and `identifier`.
         *
         * @param nodeType the class object of the requested member node
         * @param identifier the identifier of the requested member node
         * @return the requested member node, or `null` if this node doesn't
         * contain a member with the specified `nodeType` and `identifier`
         */
        fun find(nodeType: KClass<out Node>, identifier: String): Node? =
                memberMap[nodeType to identifier]

        /**
         * Returns the member with the specified type and `identifier`.
         *
         * @param T the type of the requested member node
         * @param identifier the identifier of the requested member node
         * @return the requested member node, or `null` if this node doesn't
         * contain a member with the specified type and `identifier`
         */
        inline fun <reified T : Node> find(identifier: String): T? =
                find(T::class, identifier) as T?
    }

    /**
     * A variable declared within a source file.
     *
     * The [identifier] of a variable is its [name].
     *
     * @property name the name of this variable
     * @property initializer the initializer lines of this variable, or an empty
     * list if it doesn't have an initializer
     */
    data class Variable(
            val name: String,
            val initializer: List<String> = emptyList(),
            override val properties: Map<String, String> = emptyMap()
    ) : Node() {
        override val identifier: String
            get() = name

        constructor(
                name: String,
                initializer: String?,
                properties: Map<String, String> = emptyMap()
        ) : this(
                name = name,
                initializer = initializer?.split("\n")
                        ?.filter(String::isNotBlank)
                        ?.map(String::trim)
                        ?: emptyList(),
                properties = properties
        )
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
     * level is allowed, or `name()` otherwise.
     *
     * @property signature the signature of this function
     * @property parameters the parameters of this function
     * @property body the body lines of this function, or an empty list if it
     * doesn't have a body
     * @throws IllegalArgumentException if multiple `parameters` have the same
     * `name`
     */
    data class Function(
            val signature: String,
            val parameters: List<Variable> = emptyList(),
            val body: List<String> = emptyList(),
            override val properties: Map<String, String> = emptyMap()
    ) : Node() {
        override val identifier: String
            get() = signature

        init {
            require(parameters.distinctBy { it.name }.size == parameters.size) {
                "$parameters contains duplicated parameters!"
            }
        }

        constructor(
                signature: String,
                parameters: List<Variable>,
                body: String?,
                properties: Map<String, String> = emptyMap()
        ) : this(
                signature = signature,
                parameters = parameters,
                body = body?.split("\n")
                        ?.filter(String::isNotBlank)
                        ?.map(String::trim)
                        ?: emptyList(),
                properties = properties
        )
    }
}
