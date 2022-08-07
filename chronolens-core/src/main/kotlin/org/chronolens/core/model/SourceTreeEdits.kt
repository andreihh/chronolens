/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
    /** The qualified id of the edited node. */
    public abstract val id: String

    /** The path of the [SourceFile] which contains the edited node. */
    public val sourcePath: SourcePath
        get() = id.sourcePath

    /**
     * Applies this edit to the given [sourceTree].
     *
     * @throws IllegalStateException if the [sourceTree] has an invalid state and this edit couldn't
     * be applied
     */
    public fun applyOn(sourceTree: SourceTree) {
        sourceTree.mutate { sourceMap, nodeMap ->
            val sourcePath = sourcePath
            sourceMap -= sourcePath
            applyOn(nodeMap)
            val newSource = get(sourcePath)
            if (newSource != null) {
                sourceMap[sourcePath] = newSource
            }
        }
    }

    /**
     * Applies this edit on the given mutable map of [nodes].
     *
     * @throws IllegalStateException if the [nodes] have an invalid state and this edit couldn't be
     * applied
     */
    internal abstract fun applyOn(nodes: NodeHashMap)

    public companion object {
        /**
         * Applies the given [edit] to this source tree.
         *
         * @throws IllegalStateException if this source tree has an invalid state and the given
         * [edit] couldn't be applied
         */
        @JvmStatic
        public fun SourceTree.apply(edit: SourceTreeEdit) {
            edit.applyOn(this)
        }

        /** Utility method. */
        @JvmStatic
        public fun SourceTree.apply(edits: List<SourceTreeEdit>) {
            edits.forEach { apply(it) }
        }

        /** Utility method. */
        @JvmStatic
        public fun SourceTree.apply(edits: Sequence<SourceTreeEdit>) {
            edits.forEach { apply(it) }
        }

        /** Utility method. */
        @JvmStatic
        public fun SourceTree.apply(vararg edits: SourceTreeEdit) {
            apply(edits.asList())
        }

        /**
         * Returns the edits which must be applied to [this] source tree in order to obtain the
         * [other] source tree.
         */
        @JvmStatic
        public fun SourceTree.diff(other: SourceTree): List<SourceTreeEdit> {
            val thisSources = this.sources.map(SourceFile::path)
            val otherSources = other.sources.map(SourceFile::path)
            val allSources = thisSources.union(otherSources)

            val nodesBefore = NodeHashMap()
            val nodesAfter = NodeHashMap()
            for (path in allSources) {
                this[path]?.let(nodesBefore::putSourceTree)
                other[path]?.let(nodesAfter::putSourceTree)
            }

            fun parentExists(id: String): Boolean {
                val parentId = id.parentId ?: return true
                return parentId in nodesBefore && parentId in nodesAfter
            }

            val nodeIds = nodesBefore.keys + nodesAfter.keys
            return nodeIds.filter(::parentExists).mapNotNull { id ->
                val before = nodesBefore[id]
                val after = nodesAfter[id]
                val edit =
                    when {
                        before == null && after != null -> AddNode(id, after.sourceNode)
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
 * @param T the concrete type of the added source node
 * @property node the node which should be added to the source tree
 */
public data class AddNode<T : SourceNode>(override val id: String, val node: T) : SourceTreeEdit() {
    val sourceTreeNode: SourceTreeNode<T>
        get() = SourceTreeNode(id, node)

    override fun applyOn(nodes: NodeHashMap) {
        check(id !in nodes) { "Node '$id' already exists!" }
        nodes.putSourceTree(sourceTreeNode)
        if (node is SourceEntity) {
            updateAncestors(nodes, id, node)
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
        require(QualifiedSourceNodeId.isValid(id)) { "Invalid qualified id '$id'!" }
    }

    override fun applyOn(nodes: NodeHashMap) {
        val node = nodes[id] ?: error("Node '$id' doesn't exist!")
        nodes.removeSourceTree(node)
        if (node.sourceNode is SourceEntity) {
            updateAncestors(nodes, id, node.sourceNode)
        }
    }
}

/**
 * Indicates that the properties of a [Type] within a [SourceTree] should be edited.
 *
 * @property supertypeEdits the edits which should be applied to the [Type.supertypes] of the type
 * with the given [id]
 * @property modifierEdits the edits which should be applied to the [Type.modifiers] of the type
 * with the given [id]
 * @throws IllegalArgumentException if the [id] is not a valid type id
 */
public data class EditType(
    override val id: String,
    val supertypeEdits: List<SetEdit<Identifier>> = emptyList(),
    val modifierEdits: List<SetEdit<String>> = emptyList()
) : SourceTreeEdit() {

    init {
        validateTypeId(id)
    }

    override fun applyOn(nodes: NodeHashMap) {
        val type = nodes[id]?.sourceNode as? Type? ?: error("Type '$id' doesn't exist!")
        val newType =
            type.copy(
                supertypes = type.supertypes.apply(supertypeEdits),
                modifiers = type.modifiers.apply(modifierEdits)
            )
        nodes[id] = SourceTreeNode(id, newType)
        updateAncestors(nodes, id, newType)
    }
}

/**
 * Indicates that the properties of a [Function] within a [SourceTree] should be edited.
 *
 * @property parameterEdits the edits which should be applied to the [Function.parameters] of the
 * function with the given [id]
 * @property modifierEdits the edits which should be applied to the [Function.modifiers] of the
 * function with the given [id]
 * @property bodyEdits the edits which should be applied to the [Function.body] of the function with
 * the given [id]
 * @throws IllegalArgumentException if the [id] is not a valid function id
 */
public data class EditFunction(
    override val id: String,
    val parameterEdits: List<ListEdit<Identifier>> = emptyList(),
    val modifierEdits: List<SetEdit<String>> = emptyList(),
    val bodyEdits: List<ListEdit<String>> = emptyList()
) : SourceTreeEdit() {

    init {
        validateFunctionId(id)
    }

    override fun applyOn(nodes: NodeHashMap) {
        val function = nodes[id]?.sourceNode as Function? ?: error("Function '$id' doesn't exist!")
        val newFunction =
            function.copy(
                parameters = function.parameters.apply(parameterEdits),
                modifiers = function.modifiers.apply(modifierEdits),
                body = function.body.apply(bodyEdits)
            )
        nodes[id] = SourceTreeNode(id, newFunction)
        updateAncestors(nodes, id, newFunction)
    }
}

/**
 * Indicates that the properties of a [Variable] within a [SourceTree] should be edited.
 *
 * @property modifierEdits the edits which should be applied to the [Variable.modifiers] of the
 * variable with the given [id]
 * @property initializerEdits the edits which should be applied to the [Variable.initializer] of the
 * variable with the given [id]
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
        val variable = nodes[id]?.sourceNode as? Variable? ?: error("Variable '$id' doesn't exist!")
        val newVariable =
            variable.copy(
                modifiers = variable.modifiers.apply(modifierEdits),
                initializer = variable.initializer.apply(initializerEdits)
            )
        nodes[id] = SourceTreeNode(id, newVariable)
        updateAncestors(nodes, id, newVariable)
    }
}

/**
 * Updates all the ancestors of the given modified [entity] from the given mutable map of [nodes]
 *
 * @throws IllegalStateException if the given [nodes] have an invalid state and the ancestors of the
 * given [entity] couldn't be updated
 */
private fun updateAncestors(nodes: NodeHashMap, qualifiedId: String, entity: SourceEntity) {
    fun Set<SourceEntity>.updatedWithEntity(): Set<SourceEntity> {
        val newEntities = LinkedHashSet<SourceEntity>(size)
        this.filterTo(newEntities) { it.kind != entity.kind || it.simpleId != entity.simpleId }
        if (qualifiedId in nodes) {
            newEntities += entity
        }
        return newEntities
    }

    fun SourceFile.updated() = copy(entities = entities.updatedWithEntity())
    fun Type.updated() = copy(members = members.updatedWithEntity())

    // TODO: simplify parentId computation.
    val parentId = SourceTreeNode(qualifiedId, entity).parentId
    val parent = nodes[parentId]?.sourceNode ?: error("Parent '$parentId' doesn't exist!")
    when (parent) {
        is SourceFile -> {
            nodes[parentId] = SourceTreeNode(parentId, parent.updated())
        }
        is Type -> {
            val newParent = parent.updated()
            nodes[parentId] = SourceTreeNode(parentId, newParent)
            updateAncestors(nodes, parentId, newParent)
        }
        else -> error("Unknown container '${parent::class}'!")
    }
}
