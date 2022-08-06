/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.QualifiedSourceNodeId.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.QualifiedSourceNodeId.Companion.MEMBER_SEPARATOR
import org.chronolens.core.model.SourceNodeKind.FUNCTION
import org.chronolens.core.model.SourceNodeKind.SOURCE_FILE
import org.chronolens.core.model.SourceNodeKind.TYPE
import org.chronolens.core.model.SourceNodeKind.VARIABLE

/**
 * A qualified id of a concrete [SourceNode].
 *
 * @param T the concrete type of the source node denoted by this qualified id
 * @param parent the id of the parent node of the node denoted by this qualified id, or `null` if
 * this id denotes a source file
 * @param id the simple id of the node denoted by this qualified id
 * @param nodeType the reflective instantiation of [T]
 * @throws IllegalArgumentException if this qualified id denotes a source file but [parent] is not
 * null, or if this qualified id denotes a source entity but [parent] is `null`, or if [T] denotes
 * an abstract type, or if the simple [id] is not valid for the denoted node type [T]
 */
public data class QualifiedSourceNodeId<T : SourceNode>(
    private val parent: QualifiedSourceNodeId<out SourceContainer>?,
    public val id: SourceNodeId,
    private val nodeType: Class<T>
) {

    /** The kind of the source node denoted by this qualified id. */
    public val kind: SourceNodeKind =
        when (nodeType.kotlin) {
            SourceFile::class -> SOURCE_FILE
            Type::class -> TYPE
            Function::class -> FUNCTION
            Variable::class -> VARIABLE
            else -> throw IllegalArgumentException("Invalid node type '$nodeType'!")
        }

    init {
        if (kind == SOURCE_FILE) {
            require(parent == null) { "Source file '$id' must not have a parent!" }
        } else {
            requireNotNull(parent) { "Source entity id '$id' must have a parent!" }
        }
        when (kind) {
            SOURCE_FILE -> require(id is SourcePath) { "'$id' must be a source path!" }
            TYPE,
            VARIABLE -> require(id is Identifier) { "'$id' must be an identifier!" }
            FUNCTION -> require(id is Signature) { "'$id' must be a signature!" }
        }
    }

    /** The path of the source file that contains the node denoted by this qualified id. */
    public val sourcePath: SourcePath
        get() = parent?.sourcePath ?: id as SourcePath

    /**
     * Casts this qualified id to denote a source node of type [nodeType].
     *
     * @throws IllegalArgumentException if the given [nodeType] doesn't match the node type denoted
     * by this qualified id
     */
    public fun <S : SourceNode> toTyped(nodeType: Class<S>): QualifiedSourceNodeId<S> {
        require(nodeType.isAssignableFrom(this.nodeType)) {
            "Failed to cast qualified id '$this' with kind '$kind' to denote node type '$nodeType'!"
        }
        @Suppress("UNCHECKED_CAST") return this as QualifiedSourceNodeId<S>
    }

    /**
     * Casts this qualified id to denote a source node of type [S].
     *
     * @throws IllegalArgumentException if the given node type [S] doesn't match the node type
     * denoted by this qualified id
     */
    public inline fun <reified S : SourceNode> toTyped(): QualifiedSourceNodeId<S> =
        toTyped(S::class.java)

    public override fun toString(): String {
        val builder = StringBuilder()

        fun appendId(qualifiedId: QualifiedSourceNodeId<*>) {
            qualifiedId.parent?.let(::appendId)
            val separator =
                when (qualifiedId.kind) {
                    SOURCE_FILE -> ""
                    TYPE -> CONTAINER_SEPARATOR
                    FUNCTION,
                    VARIABLE -> MEMBER_SEPARATOR
                }
            builder.append(separator)
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

        /**
         * Creates a [QualifiedSourceNodeId] denoting a source node of type [T] with the given
         * [parent] id and simple [id].
         *
         * @throws IllegalArgumentException if the created qualified id is invalid
         */
        @JvmStatic
        public inline fun <reified T : SourceNode> of(
            parent: QualifiedSourceNodeId<out SourceContainer>?,
            id: SourceNodeId
        ): QualifiedSourceNodeId<T> = QualifiedSourceNodeId(parent, id, T::class.java)

        /**
         * Parses the given [rawQualifiedId].
         *
         * @throws IllegalArgumentException if the given [rawQualifiedId] is invalid
         */
        @JvmStatic
        public fun parseFrom(rawQualifiedId: String): QualifiedSourceNodeId<*> {
            validateMemberSeparators(rawQualifiedId)
            val tokens = rawQualifiedId.split(*SEPARATORS)
            require(tokens.isNotEmpty() && tokens.all(String::isNotBlank)) {
                "Invalid qualified id '$rawQualifiedId'!"
            }

            // First token is always the source file path.
            var qualifiedId: QualifiedSourceNodeId<out SourceContainer> =
                qualifiedPathOf(tokens.first())

            // Stop if there is just one token.
            if (tokens.size == 1) return qualifiedId

            // Middle tokens are always type names.
            for (token in tokens.subList(1, tokens.size - 1)) {
                qualifiedId = qualifiedId.type(token)
            }

            // There are at least two tokens, so the separator exists.
            val separator = rawQualifiedId[rawQualifiedId.lastIndexOfAny(SEPARATORS)]
            val lastId = tokens.last()
            val isSignature = '(' in lastId && lastId.endsWith(')')
            return when {
                separator == CONTAINER_SEPARATOR -> qualifiedId.type(lastId)
                separator == MEMBER_SEPARATOR && isSignature -> qualifiedId.function(lastId)
                separator == MEMBER_SEPARATOR && !isSignature -> qualifiedId.variable(lastId)
                else -> error("Invalid separator '$separator' in '$rawQualifiedId'!")
            }
        }

        /**
         * Returns the qualified id of the parent of the node denoted by this qualified id, or
         * `null` if this id denotes a [SourceFile].
         */
        @JvmStatic
        @get:JvmName("getParentIdOrNull")
        public val QualifiedSourceNodeId<*>.parentId: QualifiedSourceNodeId<out SourceContainer>?
            get() = parent

        /** Returns the qualified id of the parent of the node denoted by this qualified id. */
        @JvmStatic
        public val QualifiedSourceNodeId<out SourceEntity>.parentId:
            QualifiedSourceNodeId<out SourceContainer>
            get() = checkNotNull(parent)
    }
}

/** Creates a qualified source path from the given [path]. */
public fun qualifiedPathOf(path: SourcePath): QualifiedSourceNodeId<SourceFile> =
    QualifiedSourceNodeId.of(null, path)

/**
 * Creates a qualified source path from the given [path].
 *
 * @throws IllegalArgumentException if the given [path] is not a valid [SourcePath]
 */
public fun qualifiedPathOf(path: String): QualifiedSourceNodeId<SourceFile> =
    qualifiedPathOf(SourcePath(path))

/** Creates a new qualified id by appending the given [Type] [name] to this qualified id. */
public fun QualifiedSourceNodeId<out SourceContainer>.type(
    name: Identifier
): QualifiedSourceNodeId<Type> = QualifiedSourceNodeId.of(this, name)

/**
 * Creates a new qualified id by appending the given [Type] [name] to this qualified id.
 *
 * @throws IllegalArgumentException if the given [name] is not a valid [Identifier]
 */
public fun QualifiedSourceNodeId<out SourceContainer>.type(
    name: String
): QualifiedSourceNodeId<Type> = type(Identifier(name))

/**
 * Creates a new qualified id by appending the given [Function] [signature] to this qualified id.
 */
public fun QualifiedSourceNodeId<out SourceContainer>.function(
    signature: Signature
): QualifiedSourceNodeId<Function> = QualifiedSourceNodeId.of(this, signature)

/**
 * Creates a new qualified id by appending the given [Function] [signature] to this qualified id.
 *
 * @throws IllegalArgumentException if the given [signature] is not a valid [Signature]
 */
public fun QualifiedSourceNodeId<out SourceContainer>.function(
    signature: String
): QualifiedSourceNodeId<Function> = function(Signature(signature))

/** Creates a new qualified id by appending the given [Variable] [name] to this qualified id. */
public fun QualifiedSourceNodeId<out SourceContainer>.variable(
    name: Identifier
): QualifiedSourceNodeId<Variable> = QualifiedSourceNodeId.of(this, name)

/**
 * Creates a new qualified id by appending the given [Variable] [name] to this qualified id.
 *
 * @throws IllegalArgumentException if the given [name] is not a valid [Identifier]
 */
public fun QualifiedSourceNodeId<out SourceContainer>.variable(
    name: String
): QualifiedSourceNodeId<Variable> = variable(Identifier(name))

private val SEPARATORS = charArrayOf(CONTAINER_SEPARATOR, MEMBER_SEPARATOR)

private fun validateMemberSeparators(rawQualifiedId: String) {
    val memberIndex = rawQualifiedId.indexOf(MEMBER_SEPARATOR)
    val nextIndex = rawQualifiedId.indexOfAny(SEPARATORS, memberIndex + 1)
    require(memberIndex == -1 || nextIndex == -1) { "Invalid qualified id '$rawQualifiedId'!" }
}

public val String.sourcePath: SourcePath
    get() {
        val where = indexOfAny(SEPARATORS)
        val rawPath = if (where == -1) this else substring(0, where)
        return SourcePath(rawPath)
    }

public val String.parentId: String?
    get() {
        val where = lastIndexOfAny(SEPARATORS)
        return if (where == -1) null else substring(0, where)
    }
