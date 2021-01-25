/*
 * Copyright 2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

/** A unique identifier of a [SourceNode] within a [SourceTree]. */
public data class QualifiedId(val parent: QualifiedId?, val id: String)

/**
 * The path of the [SourceFile] that contains the [SourceNode] denoted by [this]
 * qualified id.
 */
public val QualifiedId.sourcePath: String get() = parent?.sourcePath ?: id

/** Creates a new qualified id from the given [parent] id and [identifier]. */
public fun qualifiedIdOf(parent: QualifiedId, identifier: String): QualifiedId =
    QualifiedId(parent, identifier)

/** Creates a new qualified id from the given [path] and [identifiers]. */
public fun qualifiedIdOf(path: String, vararg identifiers: String): QualifiedId {
    var qualifier = QualifiedId(null, path)
    for (identifier in identifiers) {
        qualifier = qualifiedIdOf(qualifier, identifier)
    }
    return qualifier
}
