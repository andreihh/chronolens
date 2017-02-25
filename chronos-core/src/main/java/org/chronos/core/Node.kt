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
    /** The simple name of this node. */
    abstract val name: String

    /** The unique signature of this node. */
    abstract val signature: String

    /** Two nodes are equal if and only if they have the same [signature]. */
    final override fun equals(other: Any?): Boolean =
            other is Node && signature == other.signature

    final override fun hashCode(): Int = signature.hashCode()

    /** Returns the [signature] of this node. */
    final override fun toString(): String = signature

    /**
     * A type declared withint a source file.
     *
     * @property members the members of this type (variables, functions and
     * contained types)
     */
    class Type(
            override val name: String,
            override val signature: String,
            val members: Set<Node>
    ) : Node() {
        /**
         * Creates a type with the given `name`, `signature` and `members`.
         *
         * @throws IllegalArgumentException if multiple `members` have the same
         * signature
         */
        constructor(
                name: String,
                signature: String,
                members: Collection<Node>
        ) : this(
                name = name,
                signature = signature,
                members = members.let {
                    val setOfMembers = members.toSet()
                    require(setOfMembers.size == members.size) {
                        "$members contains duplicated nodes!"
                    }
                    setOfMembers
                }
        )
    }

    /**
     * A variable declared within a source file.
     *
     * @property initializer the canonical string representation of the
     * initializer of this variable, or `null` if it doesn't have an initializer
     */
    class Variable(
            override val name: String,
            override val signature: String,
            val initializer: String?
    ) : Node()

    /**
     * A function declared within a source file.
     *
     * @property parameters the parameters of this function
     * @property body the canonical string representation of the body of this
     * function, or `null` if it doesn't have a body
     * @throws IllegalArgumentException if multiple `parameters` have the same
     * name
     */
    class Function(
            override val name: String,
            override val signature: String,
            val parameters: List<Variable>,
            val body: String?
    ) : Node() {
        init {
            require(parameters.distinctBy { it.name }.size == parameters.size) {
                "$parameters contains duplicated parameters!"
            }
        }
    }
}
