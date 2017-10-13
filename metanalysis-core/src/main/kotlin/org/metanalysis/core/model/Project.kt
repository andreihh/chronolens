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

import org.metanalysis.core.model.SourceNode.SourceUnit

/**
 * A snapshot of a project containing a [SourceUnit] set at a specific point in
 * time.
 *
 * It indexes all contained source nodes to allow fast access by id.
 *
 * @property units the source units in this project
 */
class Project private constructor(
        private val unitMap: MutableMap<String, SourceUnit>,
        private val nodeMap: MutableMap<String, SourceNode>
) {
    companion object {
        /** Creates and returns an empty project containing no source units. */
        @JvmStatic
        fun emptyProject(): Project = Project(emptyList())
    }

    /**
     * Creates a project from the given `units`.
     *
     * @param units the source units contained by the project
     * @throws IllegalArgumentException if the given `units` contain duplicated
     * ids
     */
    constructor(units: Collection<SourceUnit>) : this(
            unitMap = units.associateByTo(hashMapOf(), SourceUnit::path),
            nodeMap = buildVisit(units).toMutableMap()
    ) {
        units.groupBy(SourceUnit::id).forEach { id, unitsWithId ->
            require(unitsWithId.size == 1) {
                "Project contains duplicated unit id '$id'!"
            }
        }
    }

    /** The source units in this project. */
    val units: Collection<SourceUnit>
        get() = unitMap.values

    /** Returns all the source nodes in this project. */
    fun findAll(): Collection<SourceNode> = nodeMap.values

    /**
     * Returns the node with the specified `id`.
     *
     * @param id the fully qualified identifier of the requested node
     * @return the requested node, or `null` if no such node was found
     * @throws IllegalArgumentException if the given `id` is not a valid node id
     */
    fun find(id: String): SourceNode? {
        validateNodeId(id)
        return nodeMap[id]
    }

    /**
     * Returns the node with the specified `id`.
     *
     * @param T the type of the requested node
     * @param id the fully qualified identifier of the requested node
     * @return the requested node, or `null` if no such node was found
     * @throws IllegalArgumentException if the given `id` is not a valid node id
     * or if the requested node is not of type `T`
     */
    @JvmName("findNode")
    inline fun <reified T : SourceNode> find(id: String): T? {
        val node = find(id)
        require(node is T?) { "'$id' is not of type '${T::class}'!" }
        return node as T?
    }

    /**
     * Applies the given `edits` to this project.
     *
     * @param edits the edits which should be applied
     * @throws IllegalStateException if this project has an invalid state and
     * the given `edits` couldn't be applied
     */
    fun apply(edits: List<ProjectEdit>) {
        for (edit in edits) {
            val parentUnitId = edit.id.parentUnitId
            unitMap -= parentUnitId
            edit.applyOn(nodeMap)
            val newParentUnit = nodeMap[parentUnitId] as SourceUnit?
            if (newParentUnit != null) {
                unitMap[parentUnitId] = newParentUnit
            }
        }
    }

    /** Utility method. */
    fun apply(vararg edits: ProjectEdit) {
        apply(edits.asList())
    }

    /** Utility method. */
    fun apply(transaction: Transaction) {
        apply(transaction.edits)
    }
}
