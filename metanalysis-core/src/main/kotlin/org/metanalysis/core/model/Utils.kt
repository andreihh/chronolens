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

@file:JvmName("Utils")
@file:JvmMultifileClass

package org.metanalysis.core.model

import org.metanalysis.core.model.SourceNode.Companion.ENTITY_SEPARATOR

/** A hash map from ids to source nodes. */
internal typealias NodeHashMap = HashMap<String, SourceNode>

/** The id of the [SourceNode] which contains [this] entity. */
val SourceEntity.parentId: String
    get() = id.substringBeforeLast(ENTITY_SEPARATOR)

/** The path of the [SourceUnit] which contains [this] node. */
val SourceNode.sourcePath: String get() = id.substringBefore(ENTITY_SEPARATOR)

/**
 * The path of the [SourceUnit] which contains the node affected by [this] edit.
 */
val ProjectEdit.sourcePath: String get() = id.substringBefore(ENTITY_SEPARATOR)

/** The child source nodes contained in [this] source node. */
val SourceNode.children: Collection<SourceEntity>
    get() = when (this) {
        is SourceUnit -> entities
        is Type -> members
        is Function -> parameters
        else -> emptyList()
    }

/**
 * Returns all the source nodes contained in [this] source tree in top-down
 * order.
 */
fun SourceNode.walkSourceTree(): List<SourceNode> {
    val nodes = arrayListOf(this)
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

internal fun buildVisit(sources: Iterable<SourceUnit>): NodeHashMap {
    val nodes = hashMapOf<String, SourceNode>()
    sources.forEach(nodes::putSourceTree)
    return nodes
}