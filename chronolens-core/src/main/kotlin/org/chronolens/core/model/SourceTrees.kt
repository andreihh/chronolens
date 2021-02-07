/*
 * Copyright 2017-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import java.util.Collections.unmodifiableCollection

/**
 * A snapshot of a source tree at a specific point in time.
 *
 * It indexes all contained source nodes to allow fast access by id.
 */
public class SourceTree private constructor(
    private val sourceMap: HashMap<String, SourceFile>,
    private val nodeMap: NodeHashMap,
) {

    /** The source files in this source tree. */
    public val sources: Collection<SourceFile>
        get() = unmodifiableCollection(sourceMap.values)

    /** Returns all the source nodes in this source tree. */
    public val sourceNodes: Iterable<SourceNode>
        get() = unmodifiableCollection(nodeMap.values)

    /**
     * Returns the node with the specified [id], or `null` if no such node was
     * found.
     */
    public operator fun get(id: String): SourceNode? = nodeMap[id]

    /**
     * Returns the node of type [T] with the specified [id].
     *
     * @throws IllegalStateException if the requested node is not of type [T],
     * or if no such node was found
     */
    @JvmName("getNode")
    public inline fun <reified T : SourceNode> get(id: String): T {
        val node = get(id)
        check(node is T) { "'$id' is not of type '${T::class}'!" }
        return node
    }

    /** Utility method. */
    public fun getSource(path: String): SourceFile? = get(path) as SourceFile?

    /**
     * Applies the given [edit] to this source tree.
     *
     * @throws IllegalStateException if this source tree has an invalid state
     * and the given [edit] couldn't be applied
     */
    public fun apply(edit: SourceTreeEdit) {
        val sourcePath = edit.sourcePath
        sourceMap -= sourcePath
        edit.applyOn(nodeMap)
        val newSource = getSource(sourcePath)
        if (newSource != null) {
            sourceMap[sourcePath] = newSource
        }
    }

    /** Utility method. */
    public fun apply(edits: List<SourceTreeEdit>) {
        edits.forEach(::apply)
    }

    /** Utility method. */
    public fun apply(vararg edits: SourceTreeEdit) {
        apply(edits.asList())
    }

    public companion object {
        /**
         * Creates and returns a source tree from the given [sources].
         *
         * @throws IllegalArgumentException if the given [sources] contain
         * duplicate ids
         */
        @JvmStatic
        public fun of(sources: Collection<SourceFile>): SourceTree {
            val sourceMap = HashMap<String, SourceFile>(sources.size)
            for (source in sources) {
                require(source.path !in sourceMap) {
                    "Source tree contains duplicate source '${source.path}'!"
                }
                sourceMap[source.path] = source
            }
            val nodeMap = buildVisit(sources)
            return SourceTree(sourceMap, nodeMap)
        }

        /** Creates and returns an empty source tree. */
        @JvmStatic
        public fun empty(): SourceTree = SourceTree(HashMap(0), NodeHashMap(0))
    }
}

/** A hash map from ids to source nodes. */
internal typealias NodeHashMap = HashMap<String, SourceNode>

/**
 * Returns all the source nodes contained in [this] source tree in top-down
 * order.
 */
public fun SourceNode.walkSourceTree(): List<SourceNode> {
    val nodes = mutableListOf(this)
    var i = 0
    while (i < nodes.size) {
        nodes += nodes[i].children
        i++
    }
    return nodes
}

internal fun NodeHashMap.putSourceTree(root: SourceNode) {
    this += root.walkSourceTree().associateBy(SourceNode::id)
}

internal fun NodeHashMap.removeSourceTree(root: SourceNode) {
    this -= root.walkSourceTree().map(SourceNode::id)
}

private fun buildVisit(sources: Iterable<SourceFile>): NodeHashMap {
    val nodes = NodeHashMap()
    sources.forEach(nodes::putSourceTree)
    return nodes
}
