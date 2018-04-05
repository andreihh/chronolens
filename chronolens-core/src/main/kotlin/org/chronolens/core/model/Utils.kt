/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.SourceNode.Companion.ENTITY_SEPARATOR

/** A hash map from ids to source nodes. */
internal typealias NodeHashMap = HashMap<String, SourceNode>

/** The id of the [SourceNode] which contains [this] entity. */
val SourceEntity.parentId: String
    get() = id.substringBeforeLast(ENTITY_SEPARATOR)

/** The path of the [SourceFile] which contains [this] node. */
val SourceNode.sourcePath: String get() = id.substringBefore(ENTITY_SEPARATOR)

/**
 * The path of the [SourceFile] which contains the node affected by [this] edit.
 */
val ProjectEdit.sourcePath: String get() = id.substringBefore(ENTITY_SEPARATOR)

/** The child source nodes contained in [this] source node. */
val SourceNode.children: Collection<SourceEntity>
    get() = when (this) {
        is SourceFile -> entities
        is Type -> members
        else -> emptySet()
    }

/**
 * Returns all the source nodes contained in [this] source tree in top-down
 * order.
 */
fun SourceNode.walkSourceTree(): List<SourceNode> {
    val nodes = mutableListOf(this)
    var i = 0
    while (i < nodes.size) {
        nodes += nodes[i].children
        i++
    }
    return nodes
}

private const val typePrefix = "@type:"
private const val returnTypePrefix = "@return:"

/** Returns the type of [this] variable, or `null` if not specified. */
val Variable.type: String?
    get() = modifiers
        .singleOrNull { it.startsWith(typePrefix) }
        ?.removePrefix(typePrefix)

/** Returns the return type of [this] function, or `null` if not specified. */
val Function.returnType: String?
    get() = modifiers
        .singleOrNull { it.startsWith(returnTypePrefix) }
        ?.removePrefix(returnTypePrefix)

/** Returns the modifier corresponding to the given [type]. */
fun typeModifierOf(type: String): String = "$typePrefix$type"

/** Returns the modifier corresponding to the given [returnType]. */
fun returnTypeModifierOf(returnType: String): String =
    "$returnTypePrefix$returnType"

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
