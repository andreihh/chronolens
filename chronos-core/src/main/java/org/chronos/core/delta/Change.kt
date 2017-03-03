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
 * An abstract change to be applied on an object.
 *
 * @param T the type of the changed object
 */
interface Change<T> {
    companion object {
        /**
         * Applies the given `change` on this object, or does nothing if
         * `change` is `null`.
         */
        fun <T> T.apply(change: Change<T>?): T =
                if (change != null) change.applyOn(this) else this

        /** Applies the given `changes` on this object. */
        fun <T> T.apply(changes: List<Change<T>>): T =
                changes.fold(this) { subject, change -> subject.apply(change) }

        fun <T> T.apply(
                firstChange: Change<T>,
                secondChange: Change<T>,
                vararg remainingChanges: Change<T>
        ): T = apply(listOf(firstChange, secondChange, *remainingChanges))
    }

    /**
     * Applies this change on the given `subject`.
     *
     * @param subject the object which should be changed
     * @return the object resulting from the applied change
     * @throws IllegalStateException if the object has an invalid state and this
     * change couldn't be applied
     */
    fun applyOn(subject: T): T
}
