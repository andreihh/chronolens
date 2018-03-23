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
 * A snapshot of a project at a specific point in time.
 *
 * It indexes all contained source nodes to allow fast access by id.
 */
class Project private constructor(
    private val sourceMap: HashMap<String, SourceFile>,
    private val nodeMap: HashMap<String, SourceNode>
) {

    /** The source files in this project. */
    val sources: Collection<SourceFile>
        get() = unmodifiableCollection(sourceMap.values)

    /** Returns all the source nodes in this project. */
    val sourceTree: Iterable<SourceNode>
        get() = unmodifiableCollection(nodeMap.values)

    /**
     * Returns the node with the specified [id], or `null` if no such node was
     * found.
     */
    operator fun get(id: String): SourceNode? = nodeMap[id]

    /**
     * Returns the node of type [T] with the specified [id], or `null` if no
     * such node was found and [T] is a nullable type.
     *
     * @throws IllegalStateException if the requested node is not of type [T]
     */
    @JvmName("getNode")
    inline fun <reified T : SourceNode?> get(id: String): T {
        val node = get(id)
        check(node is T) { "'$id' is not of type '${T::class}'!" }
        return node as T
    }

    /**
     * Applies the given [edit] to this project.
     *
     * @throws IllegalStateException if this project has an invalid state and
     * the given [edit] couldn't be applied
     */
    fun apply(edit: ProjectEdit) {
        val sourcePath = edit.sourcePath
        sourceMap -= sourcePath
        edit.applyOn(nodeMap)
        val newSource = get<SourceFile?>(sourcePath)
        if (newSource != null) {
            sourceMap[sourcePath] = newSource
        }
    }

    /** Utility method. */
    fun apply(edits: List<ProjectEdit>) {
        edits.forEach(::apply)
    }

    companion object {
        /**
         * Creates and returns a project from the given [sources].
         *
         * @throws IllegalArgumentException if the given [sources] contain
         * duplicate ids
         */
        @JvmStatic
        fun of(sources: Collection<SourceFile>): Project {
            val sourceMap = HashMap<String, SourceFile>(sources.size)
            for (source in sources) {
                require(source.id !in sourceMap) {
                    "Project contains duplicate source id '${source.id}'!"
                }
                sourceMap[source.id] = source
            }
            val nodeMap = buildVisit(sources)
            return Project(sourceMap, nodeMap)
        }

        /** Creates and returns an empty project. */
        @JvmStatic
        fun empty(): Project = Project(HashMap(0), HashMap(0))
    }
}
