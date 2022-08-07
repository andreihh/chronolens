/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.QualifiedSourceNodeId.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.QualifiedSourceNodeId.Companion.MEMBER_SEPARATOR
import org.chronolens.core.model.SourcePath.Companion.PATH_SEPARATOR

private const val separators = "$PATH_SEPARATOR$CONTAINER_SEPARATOR$MEMBER_SEPARATOR\"\\\\"

private val fileComponent = Regex("(?>[^$separators]++)")
internal val identifierRegex = Regex("(?>[^$separators()]++)")
internal val signatureRegex = Regex("(?>$identifierRegex\\([^$separators]*\\))")

private val file = Regex("$fileComponent($PATH_SEPARATOR$fileComponent)*+")
private val type = Regex("$file($CONTAINER_SEPARATOR$identifierRegex)+?")
private val function = Regex("($type|$file)$MEMBER_SEPARATOR$signatureRegex")
private val variable = Regex("($type|$file)$MEMBER_SEPARATOR$identifierRegex")

private val entity = Regex("$function|$type|$variable")
private val node = Regex("$file|$entity")

internal val sourcePathRegex = file

/**
 * Validates the given [SourceEntity] [id].
 *
 * @throws IllegalArgumentException if the given [id] is not a valid source entity id
 */
internal fun validateEntityId(id: String) {
    require(id.matches(entity)) { "Invalid entity id '$id'!" }
}

/**
 * Validates the given [SourceFile] [id].
 *
 * @throws IllegalArgumentException if the given [id] is not a valid source file id
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
