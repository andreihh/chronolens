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

@file:JvmName("Utils")
@file:JvmMultifileClass

package org.chronolens.core.model

import org.chronolens.core.model.SourceNode.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.SourceNode.Companion.MEMBER_SEPARATOR

/** A hash map from ids to source nodes. */
internal typealias NodeHashMap = HashMap<String, SourceNode>

private val separators = charArrayOf(CONTAINER_SEPARATOR, MEMBER_SEPARATOR)

public val String.sourcePath: String
    get() {
        val where = indexOfAny(separators)
        return if (where == -1) this else substring(0, where)
    }

public val String.parentId: String?
    get() {
        val where = lastIndexOfAny(separators)
        return if (where == -1) null else substring(0, where)
    }

/** The id of the [SourceNode] which contains [this] entity. */
public val SourceEntity.parentId: String get() =
    id.parentId ?: throw AssertionError("'$id' must have a parent node!")

/** The path of the [SourceFile] which contains [this] node. */
public val SourceNode.sourcePath: String get() = id.sourcePath

/**
 * The path of the [SourceFile] which contains the node affected by [this] edit.
 */
public val SourceTreeEdit.sourcePath: String get() = id.sourcePath

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

internal fun buildVisit(sources: Iterable<SourceFile>): NodeHashMap {
    val nodes = NodeHashMap()
    sources.forEach(nodes::putSourceTree)
    return nodes
}
