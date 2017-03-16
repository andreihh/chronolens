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
 * An atomic change which should be applied to a map of elements.
 *
 * @param K the type of the keys in the edited map
 * @param V the type of the values in the edited map
 */
sealed class MapEdit<K, V : Any> {
    companion object {
        /**
         * Applies the given `edits` on this map and returns the result.
         *
         * @param K the type of the keys in the edited map
         * @param V the type of the values in the edited map
         * @param edits the edits which should be applied
         * @return the edited map
         * @throws IllegalStateException if this map has an invalid state and
         * the given `edits` couldn't be applied
         */
        @JvmStatic fun <K, V : Any> Map<K, V>.apply(
                edits: List<MapEdit<K, V>>
        ): Map<K, V> = edits.fold(toMutableMap()) { map, edit ->
            edit.applyOn(map)
            map
        }

        /**
         * Returns the edits which should be applied on this map to obtain the
         * `other` map.
         *
         * @param other the map which should be obtained
         * @return the edits which should be applied on this map
         */
        @JvmStatic fun <K, V : Any> Map<K, V>.diff(
                other: Map<K, V>
        ): List<MapEdit<K, V>> {
            val removed = (this.keys - other.keys).map { key ->
                Remove<K, V>(key)
            }
            val added = (other.keys - this.keys).map { key ->
                Add(key, other.getValue(key))
            }
            val replaced = (this.keys.intersect(other.keys)).map { key ->
                Replace(key, other.getValue(key))
            }
            return added + removed + replaced
        }
    }

    /**
     * Applies this edit on the given mutable map.
     *
     * @param subject the map which should be edited
     * @throws IllegalStateException if the map has an invalid state and this
     * edit couldn't be applied
     */
    protected abstract fun applyOn(subject: MutableMap<K, V>): Unit

    /**
     * Indicates that an entry should be added to the edited map.
     *
     * @param K the type of the keys in the edited map
     * @param V the type of the values in the edited map
     * @property key the key of the entry which should be added
     * @property value the value of the entry which should be added
     */
    data class Add<K, V : Any>(val key: K, val value: V) : MapEdit<K, V>() {
        override fun applyOn(subject: MutableMap<K, V>) {
            check(subject.put(key, value) == null)
        }
    }

    /**
     * Indicates that a key should be removed from the edited map.
     *
     * @param K the type of the keys in the edited map
     * @param V the type of the values in the edited map
     * @property key the key which should be removed
     */
    data class Remove<K, V : Any>(val key: K) : MapEdit<K, V>() {
        override fun applyOn(subject: MutableMap<K, V>) {
            checkNotNull(subject.remove(key))
        }
    }

    /**
     * Indicates that the value of a key should be replaced in the edited map.
     *
     * @param K the type of the keys in the edited map
     * @param V the type of the values in the edited map
     * @property key the key whose value should be replaced
     * @property value the new value of the key
     */
    data class Replace<K, V : Any>(val key: K, val value: V) : MapEdit<K, V>() {
        override fun applyOn(subject: MutableMap<K, V>) {
            checkNotNull(subject.put(key, value))
        }
    }
}
