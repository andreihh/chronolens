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

import org.chronolens.core.model.QualifiedId.Companion
import org.chronolens.core.model.QualifiedSourceNodeId.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.QualifiedSourceNodeId.Companion.MEMBER_SEPARATOR
import org.chronolens.core.model.SourceNodeKind.FUNCTION
import org.chronolens.core.model.SourceNodeKind.SOURCE_FILE
import org.chronolens.core.model.SourceNodeKind.TYPE
import org.chronolens.core.model.SourceNodeKind.VARIABLE

public sealed interface QualifiedSourceNodeId {
    public val parent: QualifiedContainerNodeId?
    public val id: SourceNodeId
    public val kind: SourceNodeKind
    public val sourcePath: SourcePath

    public abstract override fun toString(): String

    public companion object {
        /** [Type] identifiers are separated by `:` from the parent id. */
        public const val CONTAINER_SEPARATOR: Char = ':'

        /** [Function] and [Variable] identifiers are separated by `#` from the parent id. */
        public const val MEMBER_SEPARATOR: Char = '#'

        public fun parseFrom(rawQualifiedId: String): QualifiedSourceNodeId {
            validateMemberSeparators(rawQualifiedId)
            val tokens = rawQualifiedId.split(*SEPARATORS)
            require(tokens.isNotEmpty() && tokens.all(String::isNotBlank)) {
                "Invalid qualified id '$rawQualifiedId'!"
            }

            // First token is always the source file path.
            var qualifiedId: QualifiedContainerNodeId = QualifiedSourcePath.of(tokens.first())

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
                separator == MEMBER_SEPARATOR && !isSignature -> qualifiedId.function(lastId)
                else -> error("Invalid separator '$separator' in '$rawQualifiedId'!")
            }
        }
    }
}

public sealed interface QualifiedSourceEntityId : QualifiedSourceNodeId {
    abstract override val parent: QualifiedContainerNodeId

    override val sourcePath: SourcePath
        get() = parent.sourcePath
}

public sealed interface QualifiedContainerNodeId : QualifiedSourceNodeId {
    public fun type(name: Identifier): QualifiedTypeIdentifier = QualifiedTypeIdentifier(this, name)

    public fun type(name: String): QualifiedTypeIdentifier = type(Identifier(name))

    public fun function(signature: Signature): QualifiedSignature =
        QualifiedSignature(this, signature)

    public fun function(signature: String): QualifiedSignature = function(Signature(signature))

    public fun variable(name: Identifier): QualifiedVariableIdentifier =
        QualifiedVariableIdentifier(this, name)

    public fun variable(name: String): QualifiedVariableIdentifier = variable(Identifier(name))
}

public data class QualifiedSourcePath(override val id: SourcePath) : QualifiedContainerNodeId {
    override val parent: QualifiedContainerNodeId?
        get() = null

    override val kind: SourceNodeKind
        get() = SOURCE_FILE

    override val sourcePath: SourcePath
        get() = id

    override fun toString(): String = toRawQualifiedId()

    public companion object {
        @JvmStatic public fun of(path: SourcePath): QualifiedSourcePath = QualifiedSourcePath(path)

        @JvmStatic public fun of(path: String): QualifiedSourcePath = of(SourcePath(path))
    }
}

public data class QualifiedTypeIdentifier(
    override val parent: QualifiedContainerNodeId,
    override val id: Identifier
) : QualifiedContainerNodeId, QualifiedSourceEntityId {

    override val kind: SourceNodeKind
        get() = TYPE

    override fun toString(): String = toRawQualifiedId()
}

public data class QualifiedSignature(
    override val parent: QualifiedContainerNodeId,
    override val id: Signature
) : QualifiedSourceEntityId {

    override val kind: SourceNodeKind
        get() = FUNCTION

    override fun toString(): String = toRawQualifiedId()
}

public data class QualifiedVariableIdentifier(
    override val parent: QualifiedContainerNodeId,
    override val id: Identifier
) : QualifiedSourceEntityId {

    override val kind: SourceNodeKind
        get() = VARIABLE

    override fun toString(): String = toRawQualifiedId()
}

private val SEPARATORS = charArrayOf(CONTAINER_SEPARATOR, MEMBER_SEPARATOR)

private fun validateMemberSeparators(rawQualifiedId: String) {
    val memberIndex = rawQualifiedId.indexOf(Companion.MEMBER_SEPARATOR)
    val nextIndex = rawQualifiedId.indexOfAny(SEPARATORS, memberIndex + 1)
    require(memberIndex == -1 || nextIndex == -1) { "Invalid qualified id '$rawQualifiedId'!" }
}

private fun QualifiedSourceNodeId.toRawQualifiedId(): String {
    val builder = StringBuilder()

    fun appendId(qualifiedId: QualifiedSourceNodeId) {
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
