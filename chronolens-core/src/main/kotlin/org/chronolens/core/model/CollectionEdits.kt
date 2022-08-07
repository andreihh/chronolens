/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

/**
 * An atomic change which should be applied to a list of elements.
 *
 * @param T the type of the elements of the edited list
 */
public sealed class ListEdit<T> {
    /**
     * Applies this edit on the given mutable [subject].
     *
     * @throws IllegalStateException if the [subject] has an invalid state and this edit couldn't be
     * applied
     */
    protected abstract fun applyOn(subject: MutableList<T>)

    public companion object {
        /**
         * Applies the given [edits] on [this] list and returns the result.
         *
         * @throws IllegalStateException if this list has an invalid state and the given [edits]
         * couldn't be applied
         */
        @JvmStatic
        public fun <T> List<T>.apply(edits: List<ListEdit<T>>): List<T> {
            val result = toMutableList()
            for (edit in edits) {
                edit.applyOn(result)
            }
            return result
        }

        /** Utility method. */
        @JvmStatic
        public fun <T> List<T>.apply(vararg edits: ListEdit<T>): List<T> = apply(edits.asList())
    }

    /**
     * Indicates that an element should be added to the edited list.
     *
     * @param T the type of the elements of the edited list
     * @property index the index at which the element should be inserted
     * @property value the element which should be added
     * @throws IllegalArgumentException if [index] is negative
     */
    public data class Add<T>(val index: Int, val value: T) : ListEdit<T>() {
        init {
            require(index >= 0) { "Can't add '$value' at negative index '$index'!" }
        }

        override fun applyOn(subject: MutableList<T>) {
            check(index <= subject.size) { "'$index' is out of bounds for '$subject'!" }
            subject.add(index, value)
        }
    }

    /**
     * Indicates that an element should be removed from the edited list.
     *
     * @param T the type of the elements of the edited list
     * @property index the index of the element which should be removed
     * @throws IllegalArgumentException if [index] is negative
     */
    public data class Remove<T>(val index: Int) : ListEdit<T>() {
        init {
            require(index >= 0) { "Can't remove negative index '$index'!" }
        }

        override fun applyOn(subject: MutableList<T>) {
            check(index < subject.size) { "'$index' is out of bounds for '$subject'!" }
            subject.removeAt(index)
        }
    }
}

/**
 * An atomic change which should be applied to a set of elements.
 *
 * @param T the type of the elements of the edited set
 */
public sealed class SetEdit<T> {
    /**
     * Applies this edit on the given mutable [subject].
     *
     * @throws IllegalStateException if the [subject] has an invalid state and this edit couldn't be
     * applied
     */
    protected abstract fun applyOn(subject: MutableSet<T>)

    public companion object {
        /**
         * Applies the given [edits] on [this] set and returns the result.
         *
         * @throws IllegalStateException if this set has an invalid state and the given [edits]
         * couldn't be applied
         */
        @JvmStatic
        public fun <T> Set<T>.apply(edits: List<SetEdit<T>>): Set<T> {
            val result = toMutableSet()
            for (edit in edits) {
                edit.applyOn(result)
            }
            return result
        }

        /** Utility method. */
        @JvmStatic
        public fun <T> Set<T>.apply(vararg edits: SetEdit<T>): Set<T> = apply(edits.asList())
    }

    /**
     * Indicates that an element should be added to the edited set.
     *
     * @param T the type of the elements of the edited set
     * @property value the element which should be added
     */
    public data class Add<T>(val value: T) : SetEdit<T>() {
        override fun applyOn(subject: MutableSet<T>) {
            check(subject.add(value)) {
                "Can't add '$value' because it already exists in '$subject'!"
            }
        }
    }

    /**
     * Indicates that an element should be removed from the edited set.
     *
     * @param T the type of the elements of the edited set
     * @property value the element which should be removed
     */
    public data class Remove<T>(val value: T) : SetEdit<T>() {
        override fun applyOn(subject: MutableSet<T>) {
            check(subject.remove(value)) {
                "Can't remove '$value' because it doesn't exist in '$subject'!"
            }
        }
    }
}
