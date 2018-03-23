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

private const val separators = "$PATH_SEPARATOR$ENTITY_SEPARATOR\"\\\\"
private val fileComponent = Regex("(?>[^$separators]++)")
private val identifier = Regex("(?>[^$separators()]++)")
private val signature = Regex("(?>$identifier\\([^$separators]*\\))")

private val file = Regex("$fileComponent($PATH_SEPARATOR$fileComponent)*+")
private val type = Regex("$file($ENTITY_SEPARATOR$identifier)+?")
private val function = Regex("($type|$file)$ENTITY_SEPARATOR$signature")
private val variable = Regex("($type|$file)$ENTITY_SEPARATOR$identifier")

private val entity = Regex("$function|$type|$variable")
private val node = Regex("$file|$entity")

/**
 * Validates the ids of the children of this node.
 *
 * @throws IllegalArgumentException if any child has an id which doesn't start
 * with the id of this node or if this node contains duplicated children ids
 */
internal fun SourceNode.validateChildrenIds() {
    val ids = HashSet<String>(children.size)
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
 * Validates the names of the parameters of this function.
 *
 * @throws IllegalArgumentException if this function has duplicated parameter
 * names
 */
internal fun Function.validateParameterNames() {
    val names = HashSet<String>(parameters.size)
    for (name in parameters) {
        require(name !in names) {
            "Function '$id' contains duplicated parameter '$name'!"
        }
        names += name
    }
}

/**
 * Validates the given [SourceNode] [id].
 *
 * @throws IllegalArgumentException if the given [id] is not a valid source node
 * id
 */
internal fun validateNodeId(id: String) {
    require(id.matches(node)) { "Invalid node id '$id'!" }
}

/**
 * Validates the given [SourceEntity] [id].
 *
 * @throws IllegalArgumentException if the given [id] is not a valid source
 * entity id
 */
internal fun validateEntityId(id: String) {
    require(id.matches(entity)) { "Invalid entity id '$id'!" }
}

/**
 * Validates the given [SourceFile] [id].
 *
 * @throws IllegalArgumentException if the given [id] is not a valid source file
 * id
 */
internal fun validateFileId(id: String) {
    require(id.matches(file)) { "Invalid file id '$id'!" }
}

/**
 * Validates the given [Type] [id].
 *
 * @throws IllegalArgumentException if the given [id] is not a valid type id
 */
internal fun validateTypeId(id: String) {
    require(id.matches(type)) { "Invalid type id '$id'!" }
}

/**
 * Validates the given [Function] [id].
 *
 * @throws IllegalArgumentException if the given [id] is not a valid function id
 */
internal fun validateFunctionId(id: String) {
    require(id.matches(function)) { "Invalid function id '$id'!" }
}

/**
 * Validates the given [Variable] [id].
 *
 * @throws IllegalArgumentException if the given [id] is not a valid variable id
 */
internal fun validateVariableId(id: String) {
    require(id.matches(variable)) { "Invalid variable id '$id'!" }
}
