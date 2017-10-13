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
import org.metanalysis.core.model.SourceNode.Companion.PATH_SEPARATOR
import org.metanalysis.core.model.SourceNode.SourceEntity
import org.metanalysis.core.model.SourceNode.SourceEntity.Function
import org.metanalysis.core.model.SourceNode.SourceEntity.Type
import org.metanalysis.core.model.SourceNode.SourceEntity.Variable
import org.metanalysis.core.model.SourceNode.SourceUnit

private val separators = "$PATH_SEPARATOR$ENTITY_SEPARATOR"
private val identifier = Regex("(?>[^$separators()]++)")
private val signature = Regex("(?>$identifier\\([^$separators]*\\))")

private val unit = Regex("$identifier($PATH_SEPARATOR$identifier)*+")
private val type = Regex("$unit($ENTITY_SEPARATOR$identifier)+?")
private val function = Regex("($type|$unit)$ENTITY_SEPARATOR$signature")
private val variable =
        Regex("($function|$type|$unit)$ENTITY_SEPARATOR$identifier")

private val entity = Regex("$function|$type|$variable")
private val node = Regex("$unit|$entity")

/**
 * Validates the ids of the children of this node.
 *
 * @throws IllegalArgumentException if any child has an id which doesn't start
 * with the id of this node or if this node contains duplicated children ids
 */
internal fun SourceNode.validateChildrenIds() {
    val ids = hashSetOf<String>()
    for (child in children) {
        require(child.id.startsWith(id)) {
            "Node '$id' contains invalid child id '$id'!"
        }
        require(child.id !in ids) {
            "Node '$id' contains duplicated child id '$id'!"
        }
        ids += child.id
    }
}

/**
 * Validates the given [SourceNode] `id`.
 *
 * @param id the id which should be validated
 * @throws IllegalArgumentException if the given `id` is not a valid source node
 * id
 */
internal fun validateNodeId(id: String) {
    require(id.matches(node)) { "Invalid node id '$id'!" }
}

/**
 * Validates the given [SourceEntity] `id`.
 *
 * @param id the id which should be validated
 * @throws IllegalArgumentException if the given `id` is not a valid source
 * entity id
 */
internal fun validateEntityId(id: String) {
    require(id.matches(entity)) { "Invalid entity id '$id'!" }
}

/**
 * Validates the given [SourceUnit] `id`.
 *
 * @param id the id which should be validated
 * @throws IllegalArgumentException if the given `id` is not a valid source unit
 * id
 */
internal fun validateUnitId(id: String) {
    require(id.matches(unit)) { "Invalid unit id '$id'!" }
}

/**
 * Validates the given [Type] `id`.
 *
 * @param id the id which should be validated
 * @throws IllegalArgumentException if the given `id` is not a valid type id
 */
internal fun validateTypeId(id: String) {
    require(id.matches(type)) { "Invalid type id '$id'!" }
}

/**
 * Validates the given [Function] `id`.
 *
 * @param id the id which should be validated
 * @throws IllegalArgumentException if the given `id` is not a valid function id
 */
internal fun validateFunctionId(id: String) {
    require(id.matches(function)) { "Invalid function id '$id'!" }
}

/**
 * Validates the given [Variable] `id`.
 *
 * @param id the id which should be validated
 * @throws IllegalArgumentException if the given `id` is not a valid variable id
 */
internal fun validateVariableId(id: String) {
    require(id.matches(variable)) { "Invalid variable id '$id'!" }
}
