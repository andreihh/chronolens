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

@file:JvmName("Delta")

package org.chronos.core.delta

import org.chronos.core.Node
import org.chronos.core.Node.Function
import org.chronos.core.Node.Type
import org.chronos.core.Node.Variable
import org.chronos.core.SourceFile
import org.chronos.core.delta.Change.Companion.apply
import org.chronos.core.delta.NodeChange.Add
import org.chronos.core.delta.NodeChange.ChangeNode
import org.chronos.core.delta.NodeChange.Remove
import kotlin.reflect.KClass

fun SourceFile.diff(other: SourceFile): SourceFileChange {
    val add = other.nodes.filter { node -> node !in nodes }.map(::Add)
    val remove = nodes.filter { node -> node !in other.nodes }.map { node ->
        Remove(node::class, node.key)
    }
    val change = nodes.filter { node -> node in other.nodes }.map { node ->
        val key = node.key
        when (node) {
            is Type -> null
            is Variable -> {
                val otherNode = checkNotNull(other.find<Variable>(node.name))
                if (node.initializer != otherNode.initializer)
                    ChangeNode<Variable>(
                            key = key,
                            change = VariableChange(otherNode.initializer)
                    )
                else null
            }
            is Function -> null
        }
    }.filterNotNull()
    return SourceFileChange(add + remove + change)
}

// TODO: clean-up these two methods
fun String?.apply(changes: List<BlockChange>): String? =
        changes.fold(this) { _, change ->
            when (change) {
                is BlockChange.Set -> change.statements
            }
        }

fun String?.apply(vararg changes: BlockChange): String? =
        apply(changes.asList())

internal fun Set<Node>.apply(changes: List<NodeChange>): Set<Node> {
    val members = hashMapOf<Pair<KClass<out Node>, String>, Node>()
    forEach { node -> members[node::class to node.key] = node }
    changes.forEach { change ->
        when (change) {
            is Add ->
                members[change.node::class to change.node.key] = change.node
            is Remove -> members.remove(change.type to change.key)
            is ChangeNode<*> -> {
                val hashKey = change.type to change.key
                val node = checkNotNull(members[hashKey])
                @Suppress("unchecked_cast")
                members[hashKey] = (change.change as Change<Node>).applyOn(node)
            }
        }
    }
    return members.values.toSet()
}
