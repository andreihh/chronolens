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
import org.chronos.core.delta.FunctionChange.ParameterChange.AddParameter
import org.chronos.core.delta.FunctionChange.ParameterChange.RemoveParameter
import org.chronos.core.delta.NodeChange.AddNode
import org.chronos.core.delta.NodeChange.ChangeNode
import org.chronos.core.delta.NodeChange.RemoveNode
import org.chronos.core.delta.TypeChange.SupertypeChange.AddSupertype
import org.chronos.core.delta.TypeChange.SupertypeChange.RemoveSupertype

import kotlin.reflect.KClass

/** Returns the hash key of this node. */
private val NodeChange.key: Pair<KClass<out Node>, String>
    get() = when (this) {
        is AddNode -> node::class to node.identifier
        is RemoveNode -> type to identifier
        is ChangeNode<*> -> type to identifier
    }

/**
 * Returns the change which should be applied on this source file to obtain the
 * `other` source file, or `null` if they are identical.
 *
 * @param other the source file which should be obtained
 * @return the change which should be applied on this source file
 */
fun SourceFile.diff(other: SourceFile): SourceFileChange {
    val addedNodes = other.nodes
            .filter { node -> node !in nodes }
            .map(::AddNode)
    val removedNodes = nodes
            .filter { node -> node !in other.nodes }
            .map { node -> RemoveNode(node::class, node.identifier) }
    val changedNodes = nodes
            .filter { node -> node in other.nodes }
            .map { node ->
                val otherNode = other.find(node::class, node.identifier)
                node.diff(checkNotNull(otherNode))
            }.filterNotNull()
    return SourceFileChange(addedNodes + removedNodes + changedNodes)
}

/** Utility method. */
private fun Node.diff(other: Node): ChangeNode<*>? = when (this) {
    is Type -> diff(other as Type)?.let { change ->
        ChangeNode<Type>(identifier, change)
    }
    is Variable -> diff(other as Variable)?.let { change ->
        ChangeNode<Variable>(identifier, change)
    }
    is Function -> diff(other as Function)?.let { change ->
        ChangeNode<Function>(identifier, change)
    }
}

/**
 * Returns the change which should be applied on this type to obtain the `other`
 * type, or `null` if they are identical.
 *
 * @param other the type which should be obtained
 * @return the change which should be applied on this type
 */
fun Type.diff(other: Type): TypeChange? {
    require(identifier == other.identifier)
    val addedSupertypes = other.supertypes
            .filter { supertype -> supertype !in supertypes }
            .map(::AddSupertype)
    val removedSupertypes = supertypes
            .filter { supertype -> supertype !in other.supertypes }
            .map(::RemoveSupertype)
    val supertypeChanges = removedSupertypes + addedSupertypes
    val addedMembers = other.members
            .filter { member -> member !in members }
            .map(::AddNode)
    val removedMembers = members
            .filter { member -> member !in other.members }
            .map { member -> RemoveNode(member::class, member.identifier) }
    val changedMembers = members
            .filter { member -> member in other.members }
            .map { member ->
                val otherMember = other.find(member::class, member.identifier)
                member.diff(checkNotNull(otherMember))
            }.filterNotNull()
    val memberChanges = addedMembers + removedMembers + changedMembers
    return if (supertypeChanges.isNotEmpty() || memberChanges.isNotEmpty())
        TypeChange(supertypeChanges, memberChanges)
    else null
}

/**
 * Returns the change which should be applied on this variable to obtain the
 * `other` variable, or `null` if they are identical.
 *
 * @param other the variable which should be obtained
 * @return the change which should be applied on this variable
 */
fun Variable.diff(other: Variable): VariableChange? {
    require(identifier == other.identifier)
    return if (initializer != other.initializer)
        VariableChange(other.initializer)
    else null
}

/**
 * Returns the change which should be applied on this function to obtain the
 * `other` function, or `null` if they are identical.
 *
 * @param other the function which should be obtained
 * @return the change which should be applied on this function
 */
fun Function.diff(other: Function): FunctionChange? {
    require(identifier == other.identifier)
    // TODO: optimize parameter changes
    val removedParameters = parameters.map { RemoveParameter(0) }
    val addedParameters = other.parameters.mapIndexed(::AddParameter)
    val parameterChanges = removedParameters + addedParameters
    val bodyChange =
            if (body != other.body) BlockChange.Set(other.body)
            else null
    return if (parameterChanges.isNotEmpty() || bodyChange != null)
        FunctionChange(parameterChanges, bodyChange)
    else null
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

/** Applies a list of changes on a set of nodes. */
internal fun Set<Node>.apply(changes: List<NodeChange>): Set<Node> {
    val members = hashMapOf<Pair<KClass<out Node>, String>, Node>()
    forEach { node -> members[node::class to node.identifier] = node }
    changes.forEach { change ->
        when (change) {
            is AddNode -> { members[change.key] = change.node }
            is RemoveNode -> { members.remove(change.key) }
            is ChangeNode<*> -> {
                @Suppress("unchecked_cast")
                members[change.key] = checkNotNull(members[change.key])
                        .apply(change.change as Change<Node>)
            }
        }
    }
    return members.values.toSet()
}
