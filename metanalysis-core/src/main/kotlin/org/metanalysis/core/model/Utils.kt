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
import org.metanalysis.core.model.SourceNode.SourceEntity
import org.metanalysis.core.model.SourceNode.SourceEntity.Function
import org.metanalysis.core.model.SourceNode.SourceEntity.Type
import org.metanalysis.core.model.SourceNode.SourceUnit

/** The id of the [SourceNode] which contains this entity. */
val SourceEntity.parentId: String
    get() = id.substringBeforeLast(ENTITY_SEPARATOR, missingDelimiterValue = "")

/** The id of the [SourceUnit] which contains this entity in its source tree. */
val String.parentUnitId: String
    get() = substringBefore(ENTITY_SEPARATOR)

/** The child source nodes contained in this source node. */
val SourceNode.children: Collection<SourceEntity>
    get() = when (this) {
        is SourceUnit -> entities
        is Type -> members
        is Function -> parameters
        else -> emptySet()
    }

/**
 * Returns all the source nodes contained in this source tree in top-down order.
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

internal fun MutableMap<String, SourceNode>.putSourceTree(root: SourceNode) {
    this += root.walkSourceTree().associateBy(SourceNode::id)
}

internal fun MutableMap<String, SourceNode>.removeSourceTree(root: SourceNode) {
    this -= root.walkSourceTree().map(SourceNode::id)
}

internal fun buildVisit(
        units: Collection<SourceUnit>
): MutableMap<String, SourceNode> {
    val nodes = hashMapOf<String, SourceNode>()
    units.forEach(nodes::putSourceTree)
    return nodes
}
