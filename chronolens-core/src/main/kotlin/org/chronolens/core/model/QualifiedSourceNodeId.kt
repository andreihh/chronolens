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
public data class QualifiedSourceNodeId<out T : SourceNode>(
    private val parent: QualifiedSourceNodeId<SourceContainer>?,
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
     * Casts this qualified id to denote a source node of type [nodeType], or `null` if the cast
     * fails.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <S : SourceNode> castOrNull(nodeType: Class<S>): QualifiedSourceNodeId<S>? =
        if (nodeType.isAssignableFrom(this.nodeType)) this as QualifiedSourceNodeId<S> else null

    /**
     * Casts this qualified id to denote a source node of type [nodeType].
     *
     * @throws IllegalArgumentException if the cast fails
     */
    public fun <S : SourceNode> cast(nodeType: Class<S>): QualifiedSourceNodeId<S> =
        requireNotNull(castOrNull(nodeType)) {
            "Qualified id '$this' with kind '$kind' cast to denote node type '${nodeType}' failed!"
        }

    /** Casts this qualified id to denote a source node of type [S], or `null` if the cast fails. */
    public inline fun <reified S : SourceNode> castOrNull(): QualifiedSourceNodeId<S>? =
        castOrNull(S::class.java)

    /**
     * Casts this qualified id to denote a source node of type [S].
     *
     * @throws IllegalArgumentException if the cast fails
     */
    public inline fun <reified S : SourceNode> cast(): QualifiedSourceNodeId<S> =
        cast(S::class.java)

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
            parent: QualifiedSourceNodeId<SourceContainer>?,
            id: SourceNodeId
        ): QualifiedSourceNodeId<T> = QualifiedSourceNodeId(parent, id, T::class.java)

        /** Creates a qualified source path from the given [path]. */
        @JvmStatic
        public fun of(path: SourcePath): QualifiedSourceNodeId<SourceFile> = of(null, path)

        /**
         * Creates a qualified source path from the given [path].
         *
         * @throws IllegalArgumentException if the given [path] is not a valid [SourcePath]
         */
        @JvmStatic
        public fun fromPath(path: String): QualifiedSourceNodeId<SourceFile> = of(SourcePath(path))

        /** Returns whether the given [rawQualifiedId] id valid. */
        @JvmStatic
        public fun isValid(rawQualifiedId: String): Boolean {
            // There must be at most one member separator, and it must be the last one.
            val memberSeparatorIndex = rawQualifiedId.indexOf(MEMBER_SEPARATOR)
            val nextSeparatorIndex = rawQualifiedId.indexOfAny(SEPARATORS, memberSeparatorIndex + 1)
            if (memberSeparatorIndex != -1 && nextSeparatorIndex != -1) return false

            val tokens = rawQualifiedId.split(*SEPARATORS)
            if (tokens.isEmpty() || tokens.any(String::isBlank)) return false

            // The first token always denotes a source file.
            if (!SourcePath.isValid(tokens.first())) return false

            // Middle tokens always denote types.
            for (token in tokens.drop(1).dropLast(1)) {
                if (!Identifier.isValid(token)) return false
            }

            // If there's only one token, it denotes a file for which we already validated the path.
            if (tokens.size == 1) return true

            // If there are no members, the last token denotes a type.
            if (memberSeparatorIndex == -1) {
                return Identifier.isValid(tokens.last())
            }

            // The last token denotes a member (variable or function).
            return Identifier.isValid(tokens.last()) || Signature.isValid(tokens.last())
        }

        /**
         * Parses the given [rawQualifiedId].
         *
         * @throws IllegalArgumentException if the given [rawQualifiedId] is invalid
         */
        @JvmStatic
        public fun parseFrom(rawQualifiedId: String): QualifiedSourceNodeId<*> {
            require(isValid(rawQualifiedId)) { "Invalid qualified id '$rawQualifiedId'!" }

            val tokens = rawQualifiedId.split(*SEPARATORS)

            // The first token always denotes a source file.
            var qualifiedId: QualifiedSourceNodeId<SourceContainer> = fromPath(tokens.first())

            // Stop if there is just one token.
            if (tokens.size == 1) return qualifiedId

            // Middle tokens always denote types.
            for (token in tokens.drop(1).dropLast(1)) {
                qualifiedId = qualifiedId.type(token)
            }

            // There are at least two tokens, so the separator exists.
            val separator = rawQualifiedId.last { it in SEPARATORS }
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
        public val QualifiedSourceNodeId<*>.parentId: QualifiedSourceNodeId<SourceContainer>?
            get() = parent

        /** Returns the qualified id of the parent of the node denoted by this qualified id. */
        @JvmStatic
        public val QualifiedSourceNodeId<SourceEntity>.parentId:
            QualifiedSourceNodeId<SourceContainer>
            get() = checkNotNull(parent)
    }
}

/** Creates a new qualified id by appending the given [Type] [name] to this qualified id. */
public fun QualifiedSourceNodeId<SourceContainer>.type(
    name: Identifier
): QualifiedSourceNodeId<Type> = QualifiedSourceNodeId.of(this, name)

/**
 * Creates a new qualified id by appending the given [Type] [name] to this qualified id.
 *
 * @throws IllegalArgumentException if the given [name] is not a valid [Identifier]
 */
public fun QualifiedSourceNodeId<SourceContainer>.type(name: String): QualifiedSourceNodeId<Type> =
    type(Identifier(name))

/**
 * Creates a new qualified id by appending the given [Function] [signature] to this qualified id.
 */
public fun QualifiedSourceNodeId<SourceContainer>.function(
    signature: Signature
): QualifiedSourceNodeId<Function> = QualifiedSourceNodeId.of(this, signature)

/**
 * Creates a new qualified id by appending the given [Function] [signature] to this qualified id.
 *
 * @throws IllegalArgumentException if the given [signature] is not a valid [Signature]
 */
public fun QualifiedSourceNodeId<SourceContainer>.function(
    signature: String
): QualifiedSourceNodeId<Function> = function(Signature(signature))

/** Creates a new qualified id by appending the given [Variable] [name] to this qualified id. */
public fun QualifiedSourceNodeId<SourceContainer>.variable(
    name: Identifier
): QualifiedSourceNodeId<Variable> = QualifiedSourceNodeId.of(this, name)

/**
 * Creates a new qualified id by appending the given [Variable] [name] to this qualified id.
 *
 * @throws IllegalArgumentException if the given [name] is not a valid [Identifier]
 */
public fun QualifiedSourceNodeId<SourceContainer>.variable(
    name: String
): QualifiedSourceNodeId<Variable> = variable(Identifier(name))

private val SEPARATORS = charArrayOf(CONTAINER_SEPARATOR, MEMBER_SEPARATOR)

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
