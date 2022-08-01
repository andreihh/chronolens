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

public data class QualifiedSourceNodeId<T : SourceNode>(
    private val parent: QualifiedSourceNodeId<out SourceContainer>?,
    private val id: SourceNodeId,
    private val kind: SourceNodeKind
) {

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

        @JvmStatic
        public val QualifiedSourceNodeId<SourceEntity>.parentId:
            QualifiedSourceNodeId<out SourceContainer>
            get() = parent!!

        @JvmStatic
        public val QualifiedSourceNodeId<*>.sourcePath: SourcePath
            get() = parent?.sourcePath ?: id as SourcePath

        @JvmStatic
        public fun fromPath(path: SourcePath): QualifiedSourceNodeId<SourceFile> =
            QualifiedSourceNodeId(null, path, SOURCE_FILE)

        @JvmStatic
        public fun fromPath(path: String): QualifiedSourceNodeId<SourceFile> =
            fromPath(SourcePath(path))

        @JvmStatic
        public fun parseFrom(rawQualifiedId: String): QualifiedSourceNodeId<*> {
            validateMemberSeparators(rawQualifiedId)
            val tokens = rawQualifiedId.split(*SEPARATORS)
            require(tokens.isNotEmpty() && tokens.all(String::isNotBlank)) {
                "Invalid qualified id '$rawQualifiedId'!"
            }

            // First token is always the source file path.
            var qualifiedId: QualifiedSourceNodeId<out SourceContainer> = fromPath(tokens.first())

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

        @JvmStatic
        public fun parseQualifiedSourcePathFrom(
            rawQualifiedId: String
        ): QualifiedSourceNodeId<SourceFile> {
            val qualifiedId = parseFrom(rawQualifiedId)
            require(qualifiedId.kind == SOURCE_FILE)
            @Suppress("UNCHECKED_CAST") return qualifiedId as QualifiedSourceNodeId<SourceFile>
        }

        @JvmStatic
        public fun parseQualifiedTypeNameFrom(rawQualifiedId: String): QualifiedSourceNodeId<Type> {
            val qualifiedId = parseFrom(rawQualifiedId)
            require(qualifiedId.kind == TYPE)
            @Suppress("UNCHECKED_CAST") return qualifiedId as QualifiedSourceNodeId<Type>
        }

        @JvmStatic
        public fun parseQualifiedSignatureFrom(
            rawQualifiedId: String
        ): QualifiedSourceNodeId<Function> {
            val qualifiedId = parseFrom(rawQualifiedId)
            require(qualifiedId.kind == FUNCTION)
            @Suppress("UNCHECKED_CAST") return qualifiedId as QualifiedSourceNodeId<Function>
        }

        @JvmStatic
        public fun parseQualifiedVariableNameFrom(
            rawQualifiedId: String
        ): QualifiedSourceNodeId<Variable> {
            val qualifiedId = parseFrom(rawQualifiedId)
            require(qualifiedId.kind == VARIABLE)
            @Suppress("UNCHECKED_CAST") return qualifiedId as QualifiedSourceNodeId<Variable>
        }
    }
}

public fun QualifiedSourceNodeId<out SourceContainer>.type(
    name: Identifier
): QualifiedSourceNodeId<Type> = QualifiedSourceNodeId(this, name, TYPE)

public fun QualifiedSourceNodeId<out SourceContainer>.type(
    name: String
): QualifiedSourceNodeId<Type> = type(Identifier(name))

public fun QualifiedSourceNodeId<out SourceContainer>.function(
    signature: Signature
): QualifiedSourceNodeId<Function> = QualifiedSourceNodeId(this, signature, FUNCTION)

public fun QualifiedSourceNodeId<out SourceContainer>.function(
    signature: String
): QualifiedSourceNodeId<Function> = function(Signature(signature))

public fun QualifiedSourceNodeId<out SourceContainer>.variable(
    name: Identifier
): QualifiedSourceNodeId<Variable> = QualifiedSourceNodeId(this, name, VARIABLE)

public fun QualifiedSourceNodeId<out SourceContainer>.variable(
    name: String
): QualifiedSourceNodeId<Variable> = variable(Identifier(name))

private val SEPARATORS = charArrayOf(CONTAINER_SEPARATOR, MEMBER_SEPARATOR)

private fun validateMemberSeparators(rawQualifiedId: String) {
    val memberIndex = rawQualifiedId.indexOf(Companion.MEMBER_SEPARATOR)
    val nextIndex = rawQualifiedId.indexOfAny(SEPARATORS, memberIndex + 1)
    require(memberIndex == -1 || nextIndex == -1) { "Invalid qualified id '$rawQualifiedId'!" }
}
