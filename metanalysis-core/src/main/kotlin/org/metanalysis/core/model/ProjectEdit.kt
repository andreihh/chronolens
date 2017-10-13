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

package org.metanalysis.core.model

import org.metanalysis.core.model.ListEdit.Companion.apply
import org.metanalysis.core.model.SetEdit.Companion.apply
import org.metanalysis.core.model.SourceNode.Companion.ENTITY_SEPARATOR
import org.metanalysis.core.model.SourceNode.SourceEntity
import org.metanalysis.core.model.SourceNode.SourceEntity.Function
import org.metanalysis.core.model.SourceNode.SourceEntity.Type
import org.metanalysis.core.model.SourceNode.SourceEntity.Variable
import org.metanalysis.core.model.SourceNode.SourceUnit

/** An atomic change which should be applied to a [Project]. */
sealed class ProjectEdit : Edit<Project> {
    companion object {
        /**
         * Returns the edits which must be applied to this project in order to
         * obtain the `other` project.
         */
        @JvmStatic
        fun Project.diff(other: Project): List<ProjectEdit> {
            val sources = units.map(SourceUnit::id)
                    .union(other.units.map(SourceUnit::id))
            val nodesBefore = hashMapOf<String, SourceNode>()
            val nodesAfter = hashMapOf<String, SourceNode>()
            for (path in sources) {
                find(path)?.let(nodesBefore::putSourceTree)
                other.find(path)?.let(nodesAfter::putSourceTree)
            }

            val nodeIds = nodesBefore.keys + nodesAfter.keys
            val edits = arrayListOf<ProjectEdit>()

            for (id in nodeIds.sortedBy(String::length)) {
                val before = nodesBefore[id]
                val after = nodesAfter[id]
                val edit = when {
                    before == null && after != null -> AddNode(after)
                    before != null && after == null -> RemoveNode(id)
                    else -> null
                } ?: continue
                edits += edit
                edit.applyOn(nodesBefore)
            }

            for (id in nodeIds.sortedByDescending(String::length)) {
                val before = nodesBefore[id]
                val after = nodesAfter[id]
                if (before != null && after != null) {
                    val edit = before.diff(after) ?: continue
                    edits += edit
                    edit.applyOn(nodesBefore)
                }
            }

            return edits
        }
    }

    /** The id of the edited node. */
    abstract val id: String

    /**
     * Applies this edit on the given mutable node map.
     *
     * @param nodes the node map which should be edited
     * @throws IllegalStateException if the node map has an invalid state and
     * this edit couldn't be applied
     */
    internal abstract fun applyOn(nodes: MutableMap<String, SourceNode>)

    /**
     * Updates all the ancestors of the given `entity` from the given mutable
     * node map.
     *
     * @param nodes the node map which should be updated
     * @param entity the entity which was modified
     * @throws IllegalStateException if the given node map has an invalid state
     * and the ancestors of the given `entity` couldn't be updated
     */
    protected fun updateAncestors(
            nodes: MutableMap<String, SourceNode>,
            entity: SourceEntity
    ) {
        fun <T : SourceEntity> Collection<T>.updated(entity: T): List<T> {
            val newEntities = arrayListOf<T>()
            filterTo(newEntities) { it.id != entity.id }
            if (entity.id in nodes) {
                newEntities += entity
            }
            return newEntities
        }

        fun SourceUnit.updated(): SourceUnit =
                copy(entities = entities.updated(entity))

        fun Type.updated(): Type = copy(members = members.updated(entity))

        fun Function.updated(): Function =
                copy(parameters = parameters.updated(entity as Variable))

        val parent = nodes[entity.parentId]
                ?: error("Parent '${entity.parentId}' doesn't exist!")
        val newParent = when (parent) {
            is SourceUnit -> parent.updated()
            is Type -> parent.updated()
            is Function -> parent.updated()
            else -> error("Unknown container '${this::class}'!")
        }
        nodes[parent.id] = newParent
        if (newParent is SourceEntity) {
            updateAncestors(nodes, newParent)
        }
    }

    data class AddNode(val node: SourceNode) : ProjectEdit() {
        override val id: String
            get() = node.id

        override fun applyOn(nodes: MutableMap<String, SourceNode>) {
            check(id !in nodes) { "Node '$id' already exists!" }
            nodes.putSourceTree(node)
            if (node is SourceEntity) {
                updateAncestors(nodes, node)
            }
        }
    }

    data class RemoveNode(override val id: String) : ProjectEdit() {
        init {
            validateNodeId(id)
        }

        override fun applyOn(nodes: MutableMap<String, SourceNode>) {
            val node = nodes[id] ?: error("Node '$id' doesn't exist!")
            nodes.removeSourceTree(node)
            if (node is SourceEntity) {
                updateAncestors(nodes, node)
            }
        }
    }

    data class EditType(
            override val id: String,
            val modifierEdits: List<SetEdit<String>> = emptyList(),
            val supertypeEdits: List<SetEdit<String>> = emptyList()
    ) : ProjectEdit() {
        init {
            validateTypeId(id)
        }

        override fun applyOn(nodes: MutableMap<String, SourceNode>) {
            val type = nodes[id] as? Type? ?: error("Type '$id' doesn't exist!")
            val modifiers = type.modifiers.apply(modifierEdits)
            val supertypes = type.supertypes.apply(supertypeEdits)
            val newType = Type(id, modifiers, supertypes, type.members)
            nodes[id] = newType
            updateAncestors(nodes, newType)
        }
    }

    data class EditFunction(
            override val id: String,
            val modifierEdits: List<SetEdit<String>> = emptyList(),
            val parameterEdits: List<ListEdit<String>> = emptyList(),
            val bodyEdits: List<ListEdit<String>> = emptyList()
    ) : ProjectEdit() {
        init {
            validateFunctionId(id)
        }

        override fun applyOn(nodes: MutableMap<String, SourceNode>) {
            val function = nodes[id] as Function?
                    ?: error("Function '$id' doesn't exist!")
            val modifiers = function.modifiers.apply(modifierEdits)
            val parameters = function.parameters
                    .map(Variable::name)
                    .apply(parameterEdits)
                    .map { name ->
                        nodes["$id$ENTITY_SEPARATOR$name"] as? Variable
                                ?: error("Invalid parameter '$name'!")
                    }
            val body = function.body.apply(bodyEdits)
            val newFunction = Function(id, modifiers, parameters, body)
            nodes[id] = newFunction
            updateAncestors(nodes, newFunction)
        }
    }

    data class EditVariable(
            override val id: String,
            val modifierEdits: List<SetEdit<String>> = emptyList(),
            val initializerEdits: List<ListEdit<String>> = emptyList()
    ) : ProjectEdit() {
        init {
            validateVariableId(id)
        }

        override fun applyOn(nodes: MutableMap<String, SourceNode>) {
            val variable = nodes[id] as? Variable?
                    ?: error("Variable '$id' doesn't exist!")
            val modifiers = variable.modifiers.apply(modifierEdits)
            val initializer = variable.initializer.apply(initializerEdits)
            val newVariable = Variable(id, modifiers, initializer)
            nodes[id] = newVariable
            updateAncestors(nodes, newVariable)
        }
    }
}
