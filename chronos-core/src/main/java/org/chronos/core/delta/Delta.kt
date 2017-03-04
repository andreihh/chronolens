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
import org.chronos.core.delta.FunctionTransaction.ParameterChange.AddParameter
import org.chronos.core.delta.FunctionTransaction.ParameterChange.RemoveParameter
import org.chronos.core.delta.NodeChange.AddNode
import org.chronos.core.delta.NodeChange.ChangeNode
import org.chronos.core.delta.NodeChange.RemoveNode
import org.chronos.core.delta.Transaction.Companion.apply
import org.chronos.core.delta.TypeTransaction.SupertypeChange.AddSupertype
import org.chronos.core.delta.TypeTransaction.SupertypeChange.RemoveSupertype

import kotlin.reflect.KClass

/**
 * Returns the transaction which should be applied on this source file to obtain the
 * `other` source file, or `null` if they are identical.
 *
 * @param other the source file which should be obtained
 * @return the transaction which should be applied on this source file
 */
fun SourceFile.diff(other: SourceFile): SourceFileTransaction {
    val addedNodes = other.nodes.minus(nodes).map(::AddNode)
    val removedNodes = nodes.minus(other.nodes).map { node ->
        RemoveNode(node::class, node.identifier)
    }
    val changedNodes = nodes.intersect(other.nodes).map { node ->
        val otherNode = other.find(node::class, node.identifier)
        node.diff(checkNotNull(otherNode))
    }.filterNotNull()
    return SourceFileTransaction(addedNodes + removedNodes + changedNodes)
}

/**
 * Returns the transaction which should be applied on this type to obtain the
 * `other` type, or `null` if they are identical.
 *
 * @param other the type which should be obtained
 * @return the transaction which should be applied on this type
 * @throws IllegalArgumentException if the given types have different
 * identifiers
 */
fun Type.diff(other: Type): TypeTransaction? {
    require(identifier == other.identifier)

    val addedSupertypes = other.supertypes.minus(supertypes).map(::AddSupertype)
    val removedSupertypes = supertypes.minus(other.supertypes)
            .map(::RemoveSupertype)
    val supertypeChanges = removedSupertypes + addedSupertypes

    val addedMembers = other.members.minus(members).map(::AddNode)
    val removedMembers = members.minus(other.members).map { member ->
        RemoveNode(member::class, member.identifier)
    }
    val changedMembers = members.intersect(other.members).map { member ->
        val otherMember = other.find(member::class, member.identifier)
        member.diff(checkNotNull(otherMember))
    }.filterNotNull()
    val memberChanges = addedMembers + removedMembers + changedMembers

    return if (supertypeChanges.isNotEmpty() || memberChanges.isNotEmpty())
        TypeTransaction(supertypeChanges, memberChanges)
    else null
}

/**
 * Returns the transaction which should be applied on this variable to obtain
 * the `other` variable, or `null` if they are identical.
 *
 * @param other the variable which should be obtained
 * @return the transaction which should be applied on this variable
 * @throws IllegalArgumentException if the given variables have different
 * identifiers
 */
fun Variable.diff(other: Variable): VariableTransaction? {
    require(identifier == other.identifier)
    return if (initializer != other.initializer)
        VariableTransaction(other.initializer)
    else null
}

/**
 * Returns the transaction which should be applied on this function to obtain
 * the `other` function, or `null` if they are identical.
 *
 * @param other the function which should be obtained
 * @return the transaction which should be applied on this function
 * @throws IllegalArgumentException if the given functions have different
 * identifiers
 */
fun Function.diff(other: Function): FunctionTransaction? {
    require(identifier == other.identifier)
    // TODO: optimize parameter changes
    val removedParameters = parameters.map { RemoveParameter(0) }
    val addedParameters = other.parameters.mapIndexed(::AddParameter)
    val parameterChanges = removedParameters + addedParameters
    val bodyChange =
            if (body != other.body) BlockChange.Set(other.body)
            else null
    return if (parameterChanges.isNotEmpty() || bodyChange != null)
        FunctionTransaction(parameterChanges, bodyChange)
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
    val members = associateByTo(hashMapOf()) { node ->
        node::class to node.identifier
    }
    changes.forEach { change ->
        when (change) {
            is AddNode -> { members[change.key] = change.node }
            is RemoveNode -> { members.remove(change.key) }
            is ChangeNode<*> -> {
                @Suppress("unchecked_cast")
                members[change.key] = checkNotNull(members[change.key])
                        .apply(change.transaction as Transaction<Node>)
            }
        }
    }
    return members.values.toSet()
}

/** Returns the hash key of this node. */
private val NodeChange.key: Pair<KClass<out Node>, String>
    get() = when (this) {
        is AddNode -> node::class to node.identifier
        is RemoveNode -> type to identifier
        is ChangeNode<*> -> type to identifier
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
