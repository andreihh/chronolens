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

/** Abstract metadata of a declaration within a source file. */
sealed class Node {
    /** The name of this node. */
    abstract val name: String

    /**
     * Two nodes are equal if and only if they have the same [name] and are the
     * same node subtype.
     */
    final override fun equals(other: Any?): Boolean =
            other is Node && name == other.name && javaClass == other.javaClass

    final override fun hashCode(): Int = name.hashCode()

    final override fun toString(): String = name

    /**
     * A type declared within a source file.
     *
     * @property supertypes the supertypes of this type
     * @property members the members of this type (variables, functions and
     * contained types)
     */
    class Type private constructor(
            override val name: String,
            val supertypes: Set<String>,
            val members: Set<Node>
    ) : Node() {
        companion object {
            /**
             * Creates a type with the given `name`, `supertypes` and `members`.
             *
             * @throws IllegalArgumentException if `supertypes` or `members`
             * contain duplicated elements
             */
            operator fun invoke(
                    name: String,
                    supertypes: Collection<String> = emptyList(),
                    members: Collection<Node> = emptyList()
            ): Type {
                val setOfSupertypes = supertypes.toSet()
                require(setOfSupertypes.size == supertypes.size) {
                    "$supertypes contains duplicated types!"
                }
                val setOfMembers = members.toSet()
                require(setOfMembers.size == members.size) {
                    "$members contains duplicated nodes!"
                }
                return Node.Type(name, setOfSupertypes, setOfMembers)
            }
        }
    }

    /**
     * A variable declared within a source file.
     *
     * @property initializer the canonical string representation of the
     * initializer of this variable, or `null` if it doesn't have an initializer
     */
    class Variable(
            override val name: String,
            val initializer: String? = null
    ) : Node()

    /**
     * A function declared within a source file.
     *
     * The parameters of a function must have unique names.
     *
     * The name of a function should be `name(type_1, type_2, ...)` if method
     * overloading at the type level is allowed.
     *
     * The name of a function should be `name(n)`, where n is the arity of the
     * function, if method overloading at the arity level is allowed.
     *
     * Otherwise, the name of a function should be simply `name`.
     *
     * @property parameters the parameters of this function
     * @property body the canonical string representation of the body of this
     * function, or `null` if it doesn't have a body
     */
    class Function private constructor(
            override val name: String,
            val parameters: List<Variable>,
            val body: String?
    ) : Node() {
        companion object {
            /**
             * Creates a function with the given `name`, `parameters` and
             * `body`.
             * @throws IllegalArgumentException if multiple `parameters` have
             * the same name
             */
            operator fun invoke(
                    name: String,
                    parameters: List<Variable> = emptyList(),
                    body: String? = null
            ) : Function = Function(name, parameters.toList(), body)
        }

        init {
            require(parameters.distinctBy { it.name }.size == parameters.size) {
                "$parameters contains duplicated parameters!"
            }
        }
    }
}
