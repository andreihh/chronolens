/*
 * Copyright 2017-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.model.SourceNodeKind.FUNCTION
import org.chronolens.model.SourceNodeKind.SOURCE_FILE
import org.chronolens.model.SourceNodeKind.TYPE
import org.chronolens.model.SourceNodeKind.VARIABLE

/**
 * An immutable abstract representation of a source node.
 *
 * All collections passed as constructor parameters must be unmodifiable.
 */
public sealed interface SourceNode {
  /** Returns the simple, unqualified id of this source node. */
  public val id: SourceNodeId
    get() =
      when (this) {
        is SourceFile -> path
        is Type -> name
        is Variable -> name
        is Function -> signature
      }

  /** The kind of this source node. Denotes a final, non-abstract type. */
  public val kind: SourceNodeKind
    get() =
      when (this) {
        is SourceFile -> SOURCE_FILE
        is Type -> TYPE
        is Variable -> VARIABLE
        is Function -> FUNCTION
      }

  /** The child source nodes contained in this source node. */
  public val children: Collection<SourceEntity>
    get() =
      when (this) {
        is SourceFile -> entities
        is Type -> members
        else -> emptySet()
      }
}

/** An abstract representation of a source entity within a [SourceFile]. */
public sealed interface SourceEntity : SourceNode

/** An abstract representation of a container of other [SourceEntity] nodes. */
public sealed interface SourceContainer : SourceNode

/**
 * The source code metadata of a source file.
 *
 * @property path the path of this source file
 * @property entities the source entities contained by this source file
 * @throws IllegalArgumentException if the [entities] contain duplicated ids
 */
public data class SourceFile(
  val path: SourcePath,
  val entities: Set<SourceEntity> = emptySet(),
) : SourceContainer {

  init {
    validateChildrenIds()
  }
}

/**
 * A type declaration found inside a [SourceFile].
 *
 * @property name the name of this type
 * @property supertypes the supertypes of this type
 * @property modifiers the modifiers of this type
 * @property members the members of this type (functions, variables and contained types)
 * @throws IllegalArgumentException if the ids of the [members] are not valid or if the [members]
 * contain duplicated ids
 */
public data class Type(
  val name: Identifier,
  val supertypes: Set<Identifier> = emptySet(),
  val modifiers: Set<String> = emptySet(),
  val members: Set<SourceEntity> = emptySet(),
) : SourceEntity, SourceContainer {

  init {
    validateChildrenIds()
  }
}

/**
 * A variable declaration found inside a [SourceFile].
 *
 * @property name the name of this variable
 * @property modifiers the modifiers of this variable
 * @property initializer the initializer lines of this variable, or an empty list if it doesn't have
 * an initializer
 */
public data class Variable(
  val name: Identifier,
  val modifiers: Set<String> = emptySet(),
  val initializer: List<String> = emptyList(),
) : SourceEntity

/**
 * A function declaration found inside a [SourceFile].
 *
 * The parameters of a function must have unique names.
 *
 * @property signature the signature of this function; should be `name(type_1, type_2, ...)` if
 * function overloading at the type level is allowed, or `name(n)` (where `n` is the arity of the
 * function) if function overloading at the arity level is allowed, or `name()` otherwise.
 * @property parameters the names of the parameters of this function
 * @property modifiers the modifiers of this function
 * @property body the body lines of this function, or an empty list if it doesn't have a body
 * @throws IllegalArgumentException if the [parameters] contain duplicates
 */
public data class Function(
  val signature: Signature,
  val parameters: List<Identifier> = emptyList(),
  val modifiers: Set<String> = emptySet(),
  val body: List<String> = emptyList(),
) : SourceEntity {

  init {
    validateParameterNames()
  }
}

/** The final, non-abstract type of a source node. */
public enum class SourceNodeKind {
  SOURCE_FILE,
  TYPE,
  VARIABLE,
  FUNCTION,
}

/**
 * Validates the ids of the children of this node.
 *
 * @throws IllegalArgumentException if this node contains duplicated children ids
 */
private fun SourceContainer.validateChildrenIds() {
  val ids = HashSet<Pair<SourceNodeKind, SourceNodeId>>(children.size)
  for (child in children) {
    require(child.kind != SOURCE_FILE) { "Node '$id' cannot contain source file '${child.id}'!" }
    require(child.kind to child.id !in ids) {
      "Node '$id' contains duplicate child '${child.id}' of kind '${child.kind}'!"
    }
    ids += child.kind to child.id
  }
}

/**
 * Validates the names of the parameters of this function.
 *
 * @throws IllegalArgumentException if this function has duplicated parameter names
 */
private fun Function.validateParameterNames() {
  val names = HashSet<Identifier>(parameters.size)
  for (name in parameters) {
    require(name !in names) { "Function '$signature' contains duplicated parameter '$name'!" }
    names += name
  }
}
