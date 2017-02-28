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
    override abstract fun equals(other: Any?): Boolean

    override abstract fun hashCode(): Int

    /**
     * Returns a string representation of this node suitable for debugging
     * purposes only.
     */
    override abstract fun toString(): String

    /**
     * A type declared within a source file.
     *
     * @property name the name of this type
     * @property supertypes the supertypes of this type
     * @property members the members of this type (variables, functions and
     * contained types)
     */
    class Type private constructor(
            val name: String,
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
            @JvmStatic operator fun invoke(
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

        /** Two types are equal if and only if they have the same [name]. */
        override fun equals(other: Any?): Boolean =
                other is Type && name == other.name

        override fun hashCode(): Int = name.hashCode()

        override fun toString(): String = "Type($name, $supertypes, $members)"
    }

    /**
     * A variable declared within a source file.
     *
     * @property name the name of this variable
     * @property initializer the canonical string representation of the
     * initializer of this variable, or `null` if it doesn't have an initializer
     */
    class Variable(
            val name: String,
            val initializer: String? = null
    ) : Node() {
        /** Two variables are equal if and only if they have the same [name]. */
        override fun equals(other: Any?): Boolean =
                other is Variable && name == other.name

        override fun hashCode(): Int = name.hashCode()

        override fun toString(): String {
            val initBlock = if (initializer != null) ": $initializer" else ""
            return "Variable($name$initBlock)"
        }
    }

    /**
     * A function declared within a source file.
     *
     * The parameters of a function must have unique names.
     *
     * The signature of a function should be `name(type_1, type_2, ...)` if
     * function overloading at the type level is allowed.
     *
     * The signature of a function should be `name(n)`, where n is the arity of
     * the function, if function overloading at the arity level is allowed.
     *
     * Otherwise, the signature of a function should be simply `name`.
     *
     * @property signature the signature of this function
     * @property parameters the parameters of this function
     * @property body the canonical string representation of the body of this
     * function, or `null` if it doesn't have a body
     */
    class Function private constructor(
            val signature: String,
            val parameters: List<Variable>,
            val body: String?
    ) : Node() {
        companion object {
            /**
             * Creates a function with the given `signature`, `parameters` and
             * `body`.
             * @throws IllegalArgumentException if multiple `parameters` have
             * the same name
             */
            @JvmStatic operator fun invoke(
                    signature: String,
                    parameters: List<Variable> = emptyList(),
                    body: String? = null
            ) : Function = Function(signature, parameters.toList(), body)
        }

        init {
            require(parameters.distinctBy { it.name }.size == parameters.size) {
                "$parameters contains duplicated parameters!"
            }
        }

        /**
         * Two functions are equal if and only if they have the same
         * [signature].
         */
        override fun equals(other: Any?): Boolean =
                other is Function && signature == other.signature

        override fun hashCode(): Int = signature.hashCode()

        override fun toString(): String {
            val bodyBlock = if (body != null) ": $body" else ""
            return "Function(($signature, $parameters)$bodyBlock)"
        }
    }
}
