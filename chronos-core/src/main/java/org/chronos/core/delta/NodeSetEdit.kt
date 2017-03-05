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
import org.chronos.core.Node.Function
import org.chronos.core.Node.Type
import org.chronos.core.Node.Variable
import org.chronos.core.delta.FunctionTransaction.Companion.diff
import org.chronos.core.delta.Transaction.Companion.apply
import org.chronos.core.delta.TypeTransaction.Companion.diff
import org.chronos.core.delta.VariableTransaction.Companion.diff

import kotlin.reflect.KClass

private typealias Key = Pair<KClass<out Node>, String>

private val Node.key: Key
    get() = this::class to identifier

private val NodeSetEdit.key: Pair<KClass<out Node>, String>
    get() = when (this) {
        is NodeSetEdit.Add -> node.key
        is NodeSetEdit.Remove -> type to identifier
        is NodeSetEdit.Change<*> -> type to identifier
    }

/** Utility method. */
private fun Node.diff(other: Node): NodeSetEdit.Change<*>? = when (this) {
    is Type -> diff(other as Type)?.let { transaction ->
        NodeSetEdit.Change<Type>(identifier, transaction)
    }
    is Variable -> diff(other as Variable)?.let { transaction ->
        NodeSetEdit.Change<Variable>(identifier, transaction)
    }
    is Function -> diff(other as Function)?.let { transaction ->
        NodeSetEdit.Change<Function>(identifier, transaction)
    }
}

/** An atomic change which should be applied to a set of nodes. */
sealed class NodeSetEdit {
    companion object {
        @JvmStatic fun Set<Node>.apply(edits: List<NodeSetEdit>): Set<Node> {
            val map = associateByTo(hashMapOf<Key, Node>(), Node::key)
            return edits.fold(map) { nodes, edit ->
                edit.applyOn(nodes)
                nodes
            }.values.toSet()
        }

        @JvmStatic fun Set<Node>.diff(other: Set<Node>): List<NodeSetEdit> {
            val map = other.associateByTo(hashMapOf(), Node::key)
            val added = (other - this).map(::Add)
            val removed = (this - other).map { node ->
                Remove(node::class, node.identifier)
            }
            val changed = this.intersect(other).map { node ->
                node.diff(checkNotNull(map[node.key]))
            }.filterNotNull()
            return added + removed + changed
        }
    }

    protected abstract fun applyOn(subject: MutableMap<Key, Node>): Unit

    /**
     * Indicates that a node should be added to the edited set of nodes.
     *
     * @property node the added node
     */
    data class Add(val node: Node) : NodeSetEdit() {
        override fun applyOn(subject: MutableMap<Key, Node>) {
            check(subject.put(key, node) == null)
        }
    }

    /**
     * Indicates that a node should be removed from the edited set of nodes.
     *
     * @property type the class object of the removed node
     * @property identifier the `identifier` of the removed node
     */
    data class Remove(
            val type: KClass<out Node>,
            val identifier: String
    ) : NodeSetEdit() {
        companion object {
            /** Utility factory method. */
            @JvmStatic inline operator fun <reified T : Node> invoke(
                    identifier: String
            ): Remove = Remove(T::class, identifier)
        }

        override fun applyOn(subject: MutableMap<Key, Node>) {
            checkNotNull(subject.remove(key))
        }
    }

    /**
     * Indicates that a node should be change in the edited set of nodes.
     *
     * @param T the type of the changed node
     * @property type the class object of the changed node subtype
     * @property identifier the `identifier` of the changed node
     * @property transaction the transaction which should be applied on the
     * changed node
     */
    data class Change<T : Node>(
            val type: KClass<T>,
            val identifier: String,
            val transaction: Transaction<T>
    ) : NodeSetEdit() {
        companion object {
            /** Utility factory method. */
            @JvmStatic inline operator fun <reified T : Node> invoke(
                    identifier: String,
                    transaction: Transaction<T>
            ): Change<T> = Change(T::class, identifier, transaction)
        }

        override fun applyOn(subject: MutableMap<Key, Node>) {
            subject[key] = checkNotNull(type.java.cast(subject[key]))
                    .apply(transaction)
        }
    }
}
