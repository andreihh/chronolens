/*
 * Copyright 2018-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.ListEdit.Companion.apply
import org.chronolens.core.model.SetEdit.Companion.apply

/** An atomic change which should be applied to a [SourceTree]. */
public sealed class SourceTreeEdit {
    /** The [SourceNode.id] of the edited node. */
    public abstract val id: String

    /**
     * Applies this edit on the given mutable map of [nodes].
     *
     * @throws IllegalStateException if the [nodes] have an invalid state and
     * this edit couldn't be applied
     */
    internal abstract fun applyOn(nodes: NodeHashMap)

    public companion object {
        /**
         * Returns the edits which must be applied to [this] source tree in
         * order to obtain the [other] source tree.
         */
        @JvmStatic
        public fun SourceTree.diff(other: SourceTree): List<SourceTreeEdit> {
            val thisSources = this.sources.map(SourceFile::id)
            val otherSources = other.sources.map(SourceFile::id)
            val allSources = thisSources.union(otherSources)

            val nodesBefore = NodeHashMap()
            val nodesAfter = NodeHashMap()
            for (path in allSources) {
                this.getSource(path)?.let(nodesBefore::putSourceTree)
                other.getSource(path)?.let(nodesAfter::putSourceTree)
            }

            fun parentExists(id: String): Boolean {
                val parentId = id.parentId ?: return true
                return parentId in nodesBefore && parentId in nodesAfter
            }

            val nodeIds = nodesBefore.keys + nodesAfter.keys
            return nodeIds.filter(::parentExists).mapNotNull { id ->
                val before = nodesBefore[id]
                val after = nodesAfter[id]
                val edit = when {
                    before == null && after != null -> AddNode(id, after)
                    before != null && after == null -> RemoveNode(id)
                    before != null && after != null -> before.diff(after)
                    else -> throw AssertionError("Node '$id' doesn't exist!")
                }
                edit
            }
        }
    }
}

/**
 * Indicates that a [SourceNode] should be added to a source tree.
 *
 * @property node the node which should be added to the source tree
 */
public data class AddNode(
    override val id: String,
    val node: SourceNode,
) : SourceTreeEdit() {
    override fun applyOn(nodes: NodeHashMap) {
        check(id !in nodes) { "Node '$id' already exists!" }
        nodes.putSourceTree(node)
        if (node is SourceEntity) {
            updateAncestors(nodes, node)
        }
    }
}

/**
 * Indicates that a [SourceNode] should be removed from a source tree.
 *
 * @throws IllegalArgumentException if the `id` is not a valid node id
 */
public data class RemoveNode(override val id: String) : SourceTreeEdit() {
    init {
        validateNodeId(id)
    }

    override fun applyOn(nodes: NodeHashMap) {
        val node = nodes[id] ?: error("Node '$id' doesn't exist!")
        nodes.removeSourceTree(node)
        if (node is SourceEntity) {
            updateAncestors(nodes, node)
        }
    }
}

/**
 * Indicates that the properties of a [Type] within a [SourceTree] should be
 * edited.
 *
 * @property supertypeEdits the edits which should be applied to the
 * [Type.supertypes] of the type with the given [id]
 * @property modifierEdits the edits which should be applied to the
 * [Type.modifiers] of the type with the given [id]
 * @throws IllegalArgumentException if the [id] is not a valid type id
 */
public data class EditType(
    override val id: String,
    val supertypeEdits: List<SetEdit<String>> = emptyList(),
    val modifierEdits: List<SetEdit<String>> = emptyList()
) : SourceTreeEdit() {

    init {
        validateTypeId(id)
    }

    override fun applyOn(nodes: NodeHashMap) {
        val type = nodes[id] as? Type? ?: error("Type '$id' doesn't exist!")
        val supertypes = type.supertypes.apply(supertypeEdits)
        val modifiers = type.modifiers.apply(modifierEdits)
        val newType = Type(id, supertypes, modifiers, type.members)
        nodes[id] = newType
        updateAncestors(nodes, newType)
    }
}

/**
 * Indicates that the properties of a [Function] within a [SourceTree] should be
 * edited.
 *
 * @property parameterEdits the edits which should be applied to the
 * [Function.parameters] of the function with the given [id]
 * @property modifierEdits the edits which should be applied to the
 * [Function.modifiers] of the function with the given [id]
 * @property bodyEdits the edits which should be applied to the [Function.body]
 * of the function with the given [id]
 * @throws IllegalArgumentException if the [id] is not a valid function id
 */
public data class EditFunction(
    override val id: String,
    val parameterEdits: List<ListEdit<String>> = emptyList(),
    val modifierEdits: List<SetEdit<String>> = emptyList(),
    val bodyEdits: List<ListEdit<String>> = emptyList()
) : SourceTreeEdit() {

    init {
        validateFunctionId(id)
    }

    override fun applyOn(nodes: NodeHashMap) {
        val function = nodes[id] as Function?
            ?: error("Function '$id' doesn't exist!")
        val parameters = function.parameters.apply(parameterEdits)
        val modifiers = function.modifiers.apply(modifierEdits)
        val body = function.body.apply(bodyEdits)
        val newFunction = Function(id, parameters, modifiers, body)
        nodes[id] = newFunction
        updateAncestors(nodes, newFunction)
    }
}

/**
 * Indicates that the properties of a [Variable] within a [SourceTree] should be
 * edited.
 *
 * @property modifierEdits the edits which should be applied to the
 * [Variable.modifiers] of the variable with the given [id]
 * @property initializerEdits the edits which should be applied to the
 * [Variable.initializer] of the variable with the given [id]
 * @throws IllegalArgumentException if the [id] is not a valid variable id
 */
public data class EditVariable(
    override val id: String,
    val modifierEdits: List<SetEdit<String>> = emptyList(),
    val initializerEdits: List<ListEdit<String>> = emptyList()
) : SourceTreeEdit() {

    init {
        validateVariableId(id)
    }

    override fun applyOn(nodes: NodeHashMap) {
        val variable = nodes[id] as? Variable?
            ?: error("Variable '$id' doesn't exist!")
        val modifiers = variable.modifiers.apply(modifierEdits)
        val initializer = variable.initializer.apply(initializerEdits)
        val newVariable = Variable(id, modifiers, initializer)
        nodes[id] = newVariable
        updateAncestors(nodes, newVariable)
    }
}

/**
 * Updates all the ancestors of the given modified [entity] from the given
 * mutable map of [nodes]
 *
 * @throws IllegalStateException if the given [nodes] have an invalid state and
 * the ancestors of the given [entity] couldn't be updated
 */
private fun updateAncestors(nodes: NodeHashMap, entity: SourceEntity) {
    fun <T : SourceEntity> Set<T>.updated(entity: T): Set<T> {
        val newEntities = LinkedHashSet<T>(size)
        this.filterTo(newEntities) { it.id != entity.id }
        if (entity.id in nodes) {
            newEntities += entity
        }
        return newEntities
    }

    fun SourceFile.updated() = copy(entities = entities.updated(entity))
    fun Type.updated() = copy(members = members.updated(entity))

    val parent = nodes[entity.parentId]
        ?: error("Parent '${entity.parentId}' doesn't exist!")
    val newParent = when (parent) {
        is SourceFile -> parent.updated()
        is Type -> parent.updated()
        else -> error("Unknown container '${parent::class}'!")
    }
    nodes[parent.id] = newParent
    if (newParent is SourceEntity) {
        updateAncestors(nodes, newParent)
    }
}
