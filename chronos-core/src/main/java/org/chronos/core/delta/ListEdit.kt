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

package org.chronos.core.delta

/**
 * An atomic change which should be applied to a list of elements.
 *
 * @param T the type of the elements of the edited list
 */
sealed class ListEdit<T> {
    companion object {
        /**
         * Applies the given list of `edits` on this list and returns the
         * result.
         *
         * @param T the type of the elements of the edited list
         * @param edits the edits which should be applied
         * @return the edited list
         * @throws IllegalStateException if this list has an invalid state and
         * the given `edits` couldn't be applied
         */
        @JvmStatic fun <T> List<T>.apply(edits: List<ListEdit<T>>): List<T> =
                edits.fold(toMutableList()) { list, edit ->
                    edit.applyOn(list)
                    list
                }

        @JvmStatic fun <T> List<T>.diff(other: List<T>): List<ListEdit<T>> {
            val removed = this.map { Remove<T>(0) }
            val added = other.mapIndexed(::Add)
            return removed + added
        }
    }

    protected abstract fun applyOn(subject: MutableList<T>): Unit

    /**
     * Indicates that an element should be added to the edited list.
     *
     * @param T the type of the elements of the edited list
     * @property index the index at which the element should be inserted
     * @property value the element which should be added
     */
    data class Add<T>(val index: Int, val value: T) : ListEdit<T>() {
        override fun applyOn(subject: MutableList<T>) {
            subject.add(index, value)
        }
    }

    /**
     * Indicates that an element should be removed from the edited list.
     *
     * @param T the type of the elements of the edited list
     * @property index the index of the element which should be removed
     */
    data class Remove<T>(val index: Int) : ListEdit<T>() {
        override fun applyOn(subject: MutableList<T>) {
            subject.removeAt(index)
        }
    }

    /**
     * Indicates that an element should be changed in the edited list.
     *
     * @param T the type of the elements of the edited list
     * @property index the index of the element which should be changed
     * @property value the new value of the changed element
     */
    data class Change<T>(val index: Int, val value: T) : ListEdit<T>() {
        override fun applyOn(subject: MutableList<T>) {
            subject[index] = value
        }
    }
}
