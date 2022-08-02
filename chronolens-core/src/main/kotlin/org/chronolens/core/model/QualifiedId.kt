/*
 * Copyright 2021-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.QualifiedId.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.QualifiedId.Companion.MEMBER_SEPARATOR

/** A unique identifier of a [SourceNode] within a [SourceTree]. */
public data class QualifiedId(
    val parent: QualifiedId?,
    val id: SourceNodeId,
    val isContainer: Boolean
) {

    init {
        if (isContainer) {
            require(id is SourcePath || id is Identifier) {
                "Id '$id' must be a container id (source path or identifier)!"
            }
        } else {
            require(id is Identifier || id is Signature) {
                "Id '$id' must be a member id (identifier or signature)!"
            }
        }
        if (parent == null) {
            require(id is SourcePath) { "Top level qualified id '$id' must be a source path!" }
        } else {
            require(id !is SourcePath) {
                "Id '$id' with parent '$parent' must not be a source path!"
            }
            require(parent.isContainer) { "Parent '$parent' of id '$id' must not be a signature!" }
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()

        fun appendId(qualifiedId: QualifiedId) {
            if (qualifiedId.parent != null) {
                appendId(qualifiedId.parent)
                builder.append(if (qualifiedId.isContainer) ':' else '#')
            }
            builder.append(qualifiedId.id)
        }

        appendId(this)
        return builder.toString()
    }

    public companion object {
        /** [Type] identifiers are separated by `:` from the parent id. */
        public const val CONTAINER_SEPARATOR: Char = ':'

        /** [Function] and [Variable] identifiers are separated by `#` from the parent id. */
        public const val MEMBER_SEPARATOR: Char = '#'
    }
}

/** Creates a new [SourceFile] qualified id from the given [path]. */
public fun qualifiedPathOf(path: SourcePath): QualifiedId =
    QualifiedId(null, path, isContainer = true)

/** Utility method. */
public fun qualifiedPathOf(path: String): QualifiedId = qualifiedPathOf(SourcePath(path))

/**
 * Creates a new [Type] qualified id using [this] id as a parent and the given [identifier] as the
 * type name.
 *
 * @throws IllegalStateException if [this] id qualifies a [Function]
 */
public fun QualifiedId.appendType(identifier: Identifier): QualifiedId {
    check(isContainer) { "'$this' cannot be parent of '$identifier'!" }
    return QualifiedId(this, identifier, isContainer = true)
}

/** Utility method. */
public fun QualifiedId.appendType(identifier: String): QualifiedId =
    appendType(Identifier(identifier))

/**
 * Creates a new [Function] qualified id using [this] id as a parent and the given [signature].
 *
 * @throws IllegalStateException if [this] id qualifies a [Function]
 */
public fun QualifiedId.appendFunction(signature: Signature): QualifiedId {
    check(isContainer) { "'$this' cannot be parent of '$signature'!" }
    return QualifiedId(this, signature, isContainer = false)
}

/** Utility method. */
public fun QualifiedId.appendFunction(signature: String): QualifiedId =
    appendFunction(Signature(signature))

/**
 * Creates a new [Variable] qualified id using [this] id as a parent and the given [identifier] as
 * the variable name.
 *
 * @throws IllegalStateException if [this] id qualifies a [Function]
 */
public fun QualifiedId.appendVariable(identifier: Identifier): QualifiedId {
    check(isContainer) { "'$this' cannot be parent of '$identifier'!" }
    return QualifiedId(this, identifier, isContainer = false)
}

/** Utility method. */
public fun QualifiedId.appendVariable(identifier: String): QualifiedId =
    appendVariable(Identifier(identifier))

/** The path of the [SourceFile] that contains the [SourceNode] denoted by [this] qualified id. */
public val QualifiedId.sourcePath: SourcePath
    get() = parent?.sourcePath ?: (id as SourcePath)

/**
 * Parses the given [rawQualifiedId].
 *
 * @throws IllegalArgumentException if the given [rawQualifiedId] is invalid
 */
public fun parseQualifiedIdFrom(rawQualifiedId: String): QualifiedId {
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
        separator == CONTAINER_SEPARATOR -> qualifiedId.appendType(lastId)
        separator == MEMBER_SEPARATOR && isSignature -> qualifiedId.appendFunction(lastId)
        separator == MEMBER_SEPARATOR && !isSignature -> qualifiedId.appendVariable(lastId)
        else -> error("Invalid separator '$separator' in '$rawQualifiedId'!")
    }
}

private fun validateMemberSeparators(rawQualifiedId: String) {
    val memberIndex = rawQualifiedId.indexOf(MEMBER_SEPARATOR)
    val nextIndex = rawQualifiedId.indexOfAny(SEPARATORS, memberIndex + 1)
    require(memberIndex == -1 || nextIndex == -1) { "Invalid qualified id '$rawQualifiedId'!" }
}

private val SEPARATORS = charArrayOf(CONTAINER_SEPARATOR, MEMBER_SEPARATOR)

public val String.sourcePath: String
    get() {
        val where = indexOfAny(SEPARATORS)
        return if (where == -1) this else substring(0, where)
    }

/** The path of the [SourceFile] which contains the node affected by [this] edit. */
public val SourceTreeEdit.sourcePath: SourcePath
    get() = SourcePath(id.sourcePath)

/**
 * The qualified id of the parent node of the source note denoted by [this] qualified id, or `null`
 * if this id denotes a [SourceFile].
 */
public val String.parentId: String?
    get() {
        val where = lastIndexOfAny(SEPARATORS)
        return if (where == -1) null else substring(0, where)
    }

/** The qualified id of the parent node of [this] source tree node. */
public val SourceTreeNode<out SourceEntity>.parentId: String
    get() = qualifiedId.parentId ?: error("Source entity '$qualifiedId' must have a parent!")
