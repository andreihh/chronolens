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

import org.chronos.core.Node

import kotlin.reflect.KClass

/** A change which should be applied to a set of nodes. */
sealed class NodeChange {
    /**
     * Indicates that a node should be added to a set of nodes.
     *
     * @property node the added node
     */
    data class AddNode(val node: Node) : NodeChange()

    /**
     * Indicates that a node should be removed from a set of nodes.
     *
     * @property type the class object of the removed node
     * @property identifier the `identifier` of the removed node
     */
    data class RemoveNode(
            val type: KClass<out Node>,
            val identifier: String
    ) : NodeChange() {
        companion object {
            /** Utility factory method. */
            @JvmStatic inline operator fun <reified T : Node> invoke(
                    identifier: String
            ): RemoveNode = RemoveNode(T::class, identifier)
        }
    }

    /**
     * Indicates that a node should be change in a set of nodes.
     *
     * @param T the type of the changed node
     * @property type the class object of the changed node subtype
     * @property identifier the `identifier` of the changed node
     * @property transaction the transaction which should be applied on the
     * changed node
     */
    data class ChangeNode<T : Node>(
            val type: KClass<T>,
            val identifier: String,
            val transaction: Transaction<T>
    ) : NodeChange() {
        companion object {
            /** Utility factory method. */
            @JvmStatic inline operator fun <reified T : Node> invoke(
                    identifier: String,
                    transaction: Transaction<T>
            ): ChangeNode<T> = ChangeNode(T::class, identifier, transaction)
        }
    }
}
