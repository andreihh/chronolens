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

import org.chronolens.core.model.SourceNode.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.SourceNode.Companion.MEMBER_SEPARATOR
import org.chronolens.core.model.SourceNodeKind.FUNCTION
import org.chronolens.core.model.SourceNodeKind.SOURCE_FILE
import org.chronolens.core.model.SourceNodeKind.TYPE
import org.chronolens.core.model.SourceNodeKind.VARIABLE

/** A unique identifier of a [SourceNode] within a [SourceTree]. */
public data class QualifiedId(
    val parent: QualifiedId?,
    val id: String,
    val kind: SourceNodeKind,
) {

    init {
        if (parent == null) {
            require(kind == SOURCE_FILE) {
                "Top level qualified id '$id' must be a source path!"
            }
        } else {
            require(parent.kind != FUNCTION) {
                "Parent '$parent' of qualified id '$id' must not be a function!"
            }
            require(kind != SOURCE_FILE) {
                "Qualified id '$id' with parent '$parent' must not be a file!"
            }
        }
    }

    override fun toString(): String {
        if (parent == null) return id

        val builder = StringBuilder()

        fun appendParentId(parentId: QualifiedId<*>) {
            if (parentId.parent != null) {
                appendParentId(parentId.parent)
                builder.append(':')
            }
            builder.append(parentId.id)
        }

        appendParentId(parent)
        builder.append(if (kind == TYPE) ':' else '#')
        builder.append(id)
        return builder.toString()
    }
}

/** Creates a new [SourceFile] qualified id from the given [path]. */
public fun qualifiedPathOf(path: String): QualifiedId =
    QualifiedId(null, path, SOURCE_FILE)

/**
 * Creates a new [Type] qualified id using [this] id as a parent and the given
 * [identifier] as the type name.
 *
 * @throws IllegalStateException if [this] id qualifies a [Function]
 */
public fun QualifiedId.appendType(identifier: String): QualifiedId {
    check(kind != FUNCTION) { "'$this' cannot be parent of '$identifier'!" }
    return QualifiedId(this, identifier, TYPE)
}

/**
 * Creates a new [Function] qualified id using [this] id as a parent and the
 * given [signature].
 *
 * @throws IllegalStateException if [this] id qualifies a [Function]
 */
public fun QualifiedId.appendFunction(signature: String): QualifiedId {
    check(kind != FUNCTION) { "'$this' cannot be parent of '$signature'!" }
    return QualifiedId(this, signature, VARIABLE)
}

/**
 * Creates a new [Variable] qualified id using [this] id as a parent and the
 * given [identifier] as the variable name.
 *
 * @throws IllegalStateException if [this] id qualifies a [Function]
 */
public fun QualifiedId.appendVariable(identifier: String): QualifiedId {
    check(kind != FUNCTION) { "'$this' cannot be parent of '$identifier'!" }
    return QualifiedId(this, identifier, VARIABLE)
}

/**
 * The path of the [SourceFile] that contains the [SourceNode] denoted by [this]
 * qualified id.
 */
public val QualifiedId.sourcePath: String get() = parent?.sourcePath ?: id

/**
 * Parses the given [rawQualifiedId].
 *
 * @throws IllegalArgumentException if the given [rawQualifiedId] is invalid
 */
public fun parseQualifiedIdFromString(rawQualifiedId: String): QualifiedId {
    validateMemberSeparators(rawQualifiedId)
    val tokens = rawQualifiedId.split(*SEPARATORS)
    require(tokens.isNotEmpty() && tokens.all(String::isNotBlank)) {
        "Invalid qualified id '$rawQualifiedId'!"
    }

    // First token is always the source file path.
    var qualifiedId = qualifiedPathOf(tokens.first())

    // Stop if there is just one token.
    if (tokens.size == 1) return qualifiedId

    // Middle tokens are always type names.
    for (token in tokens.subList(1, tokens.size - 1)) {
        qualifiedId = qualifiedId.appendType(token)
    }

    // There are at least two tokens, so the separator exists.
    val separator = rawQualifiedId[rawQualifiedId.lastIndexOfAny(SEPARATORS)]
    val lastId = tokens.last()
    val isSignature = '(' in lastId && lastId.endsWith(')')
    return when {
        separator == ':' -> qualifiedId.appendType(lastId)
        separator == '#' && isSignature -> qualifiedId.appendFunction(lastId)
        separator == '#' && !isSignature -> qualifiedId.appendVariable(lastId)
        else -> error("Invalid separator '$separator' in '$rawQualifiedId'!!")
    }
}

private fun validateMemberSeparators(rawQualifiedId: String) {
    val memberIndex = rawQualifiedId.indexOf('#')
    val nextIndex = rawQualifiedId.indexOfAny(SEPARATORS, memberIndex + 1)
    require(memberIndex == -1 || nextIndex == -1) {
        "Invalid qualified id '$rawQualifiedId'!"
    }
}

private val SEPARATORS = charArrayOf(':', '#')

private val separators = charArrayOf(CONTAINER_SEPARATOR, MEMBER_SEPARATOR)

public val String.sourcePath: String
    get() {
        val where = indexOfAny(separators)
        return if (where == -1) this else substring(0, where)
    }

/** The path of the [SourceFile] which contains [this] node. */
public val SourceTreeNode<*>.sourcePath: String get() = qualifiedId.sourcePath

/**
 * The path of the [SourceFile] which contains the node affected by [this] edit.
 */
public val SourceTreeEdit.sourcePath: String get() = id.sourcePath

/**
 * The qualified id of the parent node of the source note denoted by [this]
 * qualified id, or `null` if this id denotes a [SourceFile].
 */
public val String.parentId: String?
    get() {
        val where = lastIndexOfAny(separators)
        return if (where == -1) null else substring(0, where)
    }

/** The qualified id of the parent node of [this] source tree node. */
public val SourceTreeNode<out SourceEntity>.parentId: String
    get() = qualifiedId.parentId
        ?: error("Source entity '$qualifiedId' must have a parent!")
