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
public data class Qualifier(val parent: Qualifier?, val id: String)

/**
 * The path of the [SourceFile] that contains the [SourceNode] denoted by [this]
 * qualiier.
 */
public val Qualifier.sourcePath: String get() = parent?.sourcePath ?: id

/**
 * Creates a new qualifier from the given [parent] qualifier and [identifier].
 */
public fun qualifierOf(parent: Qualifier, identifier: String): Qualifier =
    Qualifier(parent, identifier)

/** Creates a new qualifier from the given [path] and [identifiers]. */
public fun qualifierOf(path: String, vararg identifiers: String): Qualifier {
    var qualifier = Qualifier(null, path)
    for (identifier in identifiers) {
        qualifier = qualifierOf(qualifier, identifier)
    }
    return qualifier
}
