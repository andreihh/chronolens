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

import java.util.Collections.unmodifiableCollection

/**
 * A snapshot of a project containing a [SourceUnit] set at a specific point in
 * time.
 *
 * It indexes all contained source nodes to allow fast access by id.
 */
class Project private constructor(
    private val unitMap: HashMap<String, SourceUnit>,
    private val nodeMap: HashMap<String, SourceNode>
) {

    /** The source units in this project. */
    val sources: Collection<SourceUnit>
        get() = unmodifiableCollection(unitMap.values)

    /** Returns all the source nodes in this project. */
    fun findAll(): Collection<SourceNode> =
        unmodifiableCollection(nodeMap.values)

    /**
     * Returns the node with the specified [id], or `null` if no such node was
     * found.
     *
     * @throws IllegalArgumentException if the given [id] is not a valid node id
     */
    fun find(id: String): SourceNode? {
        validateNodeId(id)
        return nodeMap[id]
    }

    /**
     * Returns the node with the specified [id] and type [T], or `null` if no
     * such node was found.
     *
     * @throws IllegalArgumentException if the given [id] is not a valid node id
     * or if the requested node is not of type [T]
     */
    @JvmName("findNode")
    inline fun <reified T : SourceNode> find(id: String): T? {
        val node = find(id)
        require(node is T?) { "'$id' is not of type '${T::class}'!" }
        return node as T?
    }

    /**
     * Applies the given [edits] to this project.
     *
     * @throws IllegalStateException if this project has an invalid state and
     * the given [edits] couldn't be applied
     */
    fun apply(edits: List<ProjectEdit>) {
        for (edit in edits) {
            val parentUnitId = edit.sourcePath
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

    companion object {
        /**
         * Creates and returns a project from the given source [units].
         *
         * @throws IllegalArgumentException if the given [units] contain
         * duplicate ids
         */
        @JvmStatic
        fun of(units: Collection<SourceUnit>): Project {
            val unitMap = hashMapOf<String, SourceUnit>()
            for (unit in units) {
                require(unit.id !in unitMap) {
                    "Project contains duplicate unit id '${unit.id}'!"
                }
                unitMap[unit.id] = unit
            }
            val nodeMap = HashMap(buildVisit(units))
            return Project(unitMap, nodeMap)
        }

        /** Creates and returns an empty project. */
        @JvmStatic
        fun empty(): Project = Project(hashMapOf(), hashMapOf())
    }
}
