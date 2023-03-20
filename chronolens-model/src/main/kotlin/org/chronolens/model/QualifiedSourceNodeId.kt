/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.model

import org.chronolens.model.QualifiedSourceNodeId.Companion.CONTAINER_SEPARATOR
import org.chronolens.model.QualifiedSourceNodeId.Companion.MEMBER_SEPARATOR

/**
 * A qualified id of a concrete [SourceNode].
 *
 * @param T the type of the source node denoted by this qualified id
 */
public sealed interface QualifiedSourceNodeId<out T : SourceNode> {
  /** The simple id of the node denoted by this qualified id. */
  public val id: SourceNodeId

  /** The kind of the source node denoted by this qualified id. */
  public val kind: SourceNodeKind
    get() =
      when (this) {
        is SourcePath -> SourceNodeKind.SOURCE_FILE
        is QualifiedTypeIdentifier -> SourceNodeKind.TYPE
        is QualifiedVariableIdentifier -> SourceNodeKind.VARIABLE
        is QualifiedSignature -> SourceNodeKind.FUNCTION
      }

  /** The path of the source file that contains the node denoted by this qualified id. */
  public val sourcePath: SourcePath
    get() =
      when (this) {
        is SourcePath -> this
        is QualifiedTypeIdentifier -> parent.sourcePath
        is QualifiedVariableIdentifier -> parent.sourcePath
        is QualifiedSignature -> parent.sourcePath
      }

  public companion object {
    /** [Type] identifiers are separated from the parent id by `:`. */
    public const val CONTAINER_SEPARATOR: Char = ':'

    /** [Function] and [Variable] identifiers are separated from the parent id by `#`. */
    public const val MEMBER_SEPARATOR: Char = '#'

    private val ID_SEPARATORS = charArrayOf(CONTAINER_SEPARATOR, MEMBER_SEPARATOR)

    /** Returns whether the given [rawQualifiedId] id valid. */
    @JvmStatic
    public fun isValid(rawQualifiedId: String): Boolean {
      // There must be at most one member separator, and it must be the last one.
      val memberSeparatorIndex = rawQualifiedId.indexOf(MEMBER_SEPARATOR)
      val nextSeparatorIndex = rawQualifiedId.indexOfAny(ID_SEPARATORS, memberSeparatorIndex + 1)
      if (memberSeparatorIndex != -1 && nextSeparatorIndex != -1) return false

      val tokens = rawQualifiedId.split(*ID_SEPARATORS)

      // There is always at least one token. None of the tokens can be blank.
      if (tokens.any(String::isBlank)) return false

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

      val tokens = rawQualifiedId.split(*ID_SEPARATORS)

      // The first token always denotes a source file.
      var qualifiedId: QualifiedSourceNodeId<SourceContainer> = SourcePath(tokens.first())

      // Stop if there is just one token.
      if (tokens.size == 1) return qualifiedId

      // Middle tokens always denote types.
      for (token in tokens.drop(1).dropLast(1)) {
        qualifiedId = qualifiedId.type(token)
      }

      // There are at least two tokens, so the separator exists.
      val separator = rawQualifiedId.last { it in ID_SEPARATORS }
      val lastId = tokens.last()
      val isSignature = Signature.isValid(lastId)
      return when {
        separator == CONTAINER_SEPARATOR -> qualifiedId.type(lastId)
        separator == MEMBER_SEPARATOR && isSignature -> qualifiedId.function(lastId)
        separator == MEMBER_SEPARATOR && !isSignature -> qualifiedId.variable(lastId)
        else -> throw AssertionError("Invalid separator '$separator' in '$rawQualifiedId'!")
      }
    }
  }
}

private data class QualifiedTypeIdentifier(
  val parent: QualifiedSourceNodeId<SourceContainer>,
  override val id: Identifier
) : QualifiedSourceNodeId<Type> {

  override fun toString(): String = "$parent$CONTAINER_SEPARATOR$id"
}

private data class QualifiedVariableIdentifier(
  val parent: QualifiedSourceNodeId<SourceContainer>,
  override val id: Identifier
) : QualifiedSourceNodeId<Variable> {

  override fun toString(): String = "$parent$MEMBER_SEPARATOR$id"
}

private data class QualifiedSignature(
  val parent: QualifiedSourceNodeId<SourceContainer>,
  override val id: Signature
) : QualifiedSourceNodeId<Function> {

  override fun toString(): String = "$parent$MEMBER_SEPARATOR$id"
}

/** Returns the qualified id of the parent of the node denoted by this qualified id. */
public val QualifiedSourceNodeId<SourceEntity>.parentId: QualifiedSourceNodeId<SourceContainer>
  get() =
    when (this) {
      is QualifiedTypeIdentifier -> parent
      is QualifiedVariableIdentifier -> parent
      is QualifiedSignature -> parent
      else ->
        throw AssertionError("Qualified source node id '$this' does not denote a source entity!")
    }

/** Returns the name of this qualified type id. */
@get:JvmName("getTypeName")
public val QualifiedSourceNodeId<Type>.name: Identifier
  get() = id as Identifier

/** Returns the name of this qualified variable id. */
@get:JvmName("getVariableName")
public val QualifiedSourceNodeId<Variable>.name: Identifier
  get() = id as Identifier

/** Returns the signature of this qualified function id. */
@get:JvmName("getFunctionSignature")
public val QualifiedSourceNodeId<Function>.signature: Signature
  get() = id as Signature

/** Creates a new qualified id by appending the given [Type] [name] to this qualified id. */
public fun QualifiedSourceNodeId<SourceContainer>.type(
  name: Identifier
): QualifiedSourceNodeId<Type> = QualifiedTypeIdentifier(this, name)

/**
 * Creates a new qualified id by appending the given [Type] [name] to this qualified id.
 *
 * @throws IllegalArgumentException if the given [name] is not a valid [Identifier]
 */
public fun QualifiedSourceNodeId<SourceContainer>.type(name: String): QualifiedSourceNodeId<Type> =
  type(Identifier(name))

/** Creates a new qualified id by appending the given [Variable] [name] to this qualified id. */
public fun QualifiedSourceNodeId<SourceContainer>.variable(
  name: Identifier
): QualifiedSourceNodeId<Variable> = QualifiedVariableIdentifier(this, name)

/**
 * Creates a new qualified id by appending the given [Variable] [name] to this qualified id.
 *
 * @throws IllegalArgumentException if the given [name] is not a valid [Identifier]
 */
public fun QualifiedSourceNodeId<SourceContainer>.variable(
  name: String
): QualifiedSourceNodeId<Variable> = variable(Identifier(name))

/**
 * Creates a new qualified id by appending the given [Function] [signature] to this qualified id.
 */
public fun QualifiedSourceNodeId<SourceContainer>.function(
  signature: Signature
): QualifiedSourceNodeId<Function> = QualifiedSignature(this, signature)

/**
 * Creates a new qualified id by appending the given [Function] [signature] to this qualified id.
 *
 * @throws IllegalArgumentException if the given [signature] is not a valid [Signature]
 */
public fun QualifiedSourceNodeId<SourceContainer>.function(
  signature: String
): QualifiedSourceNodeId<Function> = function(Signature(signature))

/**
 * Casts this qualified id to denote a source node of type [nodeType], or `null` if the cast fails.
 */
@Suppress("UNCHECKED_CAST")
public fun <S : SourceNode> QualifiedSourceNodeId<*>.castOrNull(
  nodeType: Class<S>
): QualifiedSourceNodeId<S>? {
  val thisNodeType =
    when (this) {
      is SourcePath -> SourceFile::class.java
      is QualifiedTypeIdentifier -> Type::class.java
      is QualifiedVariableIdentifier -> Variable::class.java
      is QualifiedSignature -> Function::class.java
    }
  return if (nodeType.isAssignableFrom(thisNodeType)) this as QualifiedSourceNodeId<S> else null
}

/**
 * Casts this qualified id to denote a source node of type [nodeType].
 *
 * @throws IllegalArgumentException if the cast fails
 */
public fun <S : SourceNode> QualifiedSourceNodeId<*>.cast(
  nodeType: Class<S>
): QualifiedSourceNodeId<S> =
  requireNotNull(castOrNull(nodeType)) {
    "Qualified id '$this' with kind '$kind' cast to denote node type '${nodeType}' failed!"
  }

/** Casts this qualified id to denote a source node of type [S], or `null` if the cast fails. */
public inline fun <reified S : SourceNode> QualifiedSourceNodeId<*>.castOrNull():
  QualifiedSourceNodeId<S>? = castOrNull(S::class.java)

/**
 * Casts this qualified id to denote a source node of type [S].
 *
 * @throws IllegalArgumentException if the cast fails
 */
public inline fun <reified S : SourceNode> QualifiedSourceNodeId<*>.cast():
  QualifiedSourceNodeId<S> = cast(S::class.java)
