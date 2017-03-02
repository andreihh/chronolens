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
/*import org.chronos.core.delta.NodeChange.AddFunction
import org.chronos.core.delta.NodeChange.AddType
import org.chronos.core.delta.NodeChange.AddVariable
import org.chronos.core.delta.NodeChange.ChangeFunction*/
import org.chronos.core.delta.NodeChange.ChangeNode
//import org.chronos.core.delta.NodeChange.ChangeType
//import org.chronos.core.delta.NodeChange.ChangeVariable
import org.chronos.core.delta.NodeChange.Remove
/*import org.chronos.core.delta.NodeChange.RemoveFunction
import org.chronos.core.delta.NodeChange.RemoveType
import org.chronos.core.delta.NodeChange.RemoveVariable*/
import kotlin.reflect.KClass

/*fun SourceFile.diff(other: SourceFile): SourceFileChange {
    val add = other.nodes.filter { node -> node !in nodes }.map { node ->
        when (node) {
            is Type -> AddType(node)
            is Variable -> AddVariable(node)
            is Function -> AddFunction(node)
        }
    }
    val remove = nodes.filter { node -> node !in other.nodes }.map { node ->
        when (node) {
            is Type -> RemoveType(node.name)
            is Variable -> RemoveVariable(node.name)
            is Function -> RemoveFunction(node.signature)
        }
    }
    val change = nodes.filter { node -> node in other.nodes }.map { node ->
        when (node) {
            is Type -> null
            is Variable -> {
                val name = node.name
                val otherNode = checkNotNull(other.find<Variable>(node.name))
                if (node.initializer != otherNode.initializer)
                    ChangeVariable(otherNode.initializer)
                else null
            }
            is Function -> null
        }
    }.filterNotNull()
    TODO()
}*/

fun Node.diff(other: Node): Unit? = TODO()

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
    /*val types = hashMapOf<String, Type>()
    val variables = hashMapOf<String, Variable>()
    val functions = hashMapOf<String, Function>()*/
    forEach { node ->
        members[node::class to node.key] = node
        /*when (node) {
            is Type -> types[node.name] = node
            is Variable -> variables[node.name] = node
            is Function -> functions[node.signature] = node
        }*/
    }
    changes.forEach { change ->
        when (change) {
            /*is AddType -> types[change.type.name] = change.type
            is AddVariable -> variables[change.variable.name] = change.variable
            is AddFunction ->
                functions[change.function.signature] = change.function
            is RemoveType -> types.remove(change.name)
            is RemoveVariable -> variables.remove(change.name)
            is RemoveFunction -> functions.remove(change.signature)*/
            is Add -> members[change.node::class to change.node.key] = change.node
            is Remove -> members.remove(change.type to change.key)
            is ChangeNode<*> -> {
                val hashKey = change.type to change.key
                val node = checkNotNull(members[hashKey])
                @Suppress("unchecked_cast")
                members[hashKey] = (change.change as Change<Node>).applyOn(node)
            }
            /*is ChangeType -> types[change.name] = checkNotNull(
                    types[change.name]
            ).apply(change.typeChange)
            is ChangeVariable -> variables[change.name] = checkNotNull(
                    variables[change.name]
            ).apply(change.variableChange)
            is ChangeFunction -> functions[change.signature] = checkNotNull(
                    functions[change.signature]
            ).apply(change.functionChange)*/
        }
    }
    //val members = types.values + variables.values + functions.values
    return members.values.toSet()
}
