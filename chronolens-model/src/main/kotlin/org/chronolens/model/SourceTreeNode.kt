/*
 * Copyright 2022-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

/**
 * A fully qualified [SourceNode] within a source tree.
 *
 * @param T the type of the wrapped source node
 * @param qualifiedId the fully qualified id of the source node that uniquely identifies it within
 * the source tree
 * @param sourceNode the wrapped source node
 * @throws IllegalArgumentException if the [qualifiedId] and [sourceNode] kinds do not match
 */
public data class SourceTreeNode<out T : SourceNode>(
  val qualifiedId: QualifiedSourceNodeId<T>,
  val sourceNode: T
) {

  init {
    require(qualifiedId.id == sourceNode.id) {
      "Qualified id '$qualifiedId' must match source node id '${sourceNode.id}'!"
    }
    require(qualifiedId.kind == sourceNode.kind) {
      "Qualified id '$qualifiedId' must match source node kind '${sourceNode.kind}'!"
    }
  }

  /** The kind of this source tree node. */
  val kind: SourceNodeKind
    get() = sourceNode.kind

  /** The path of the [SourceFile] which contains this node. */
  public val sourcePath: SourcePath
    get() = qualifiedId.sourcePath

  /** Returns all the source tree nodes contained in this source subtree in top-down order. */
  public fun walkSourceTree(): List<SourceTreeNode<*>> {
    val nodes = mutableListOf<SourceTreeNode<*>>(this)
    var i = 0
    while (i < nodes.size) {
      val sourceTreeNode = nodes[i++].castOrNull<SourceContainer>() ?: continue
      for (child in sourceTreeNode.sourceNode.children) {
        nodes += sourceTreeNode.append(child)
      }
    }
    return nodes
  }

  /**
   * Casts this source tree node to denote a source node of type [nodeType], or `null` if the cast
   * fails.
   */
  @Suppress("UNCHECKED_CAST")
  public fun <S : SourceNode> castOrNull(nodeType: Class<S>): SourceTreeNode<S>? =
    if (nodeType.isAssignableFrom(sourceNode.javaClass)) this as SourceTreeNode<S> else null

  /**
   * Casts this source tree node to denote a source node of type [nodeType].
   *
   * @throws IllegalArgumentException if the cast fails
   */
  public fun <S : SourceNode> cast(nodeType: Class<S>): SourceTreeNode<S> =
    requireNotNull(castOrNull(nodeType)) {
      "Source tree node '$this' cast to denote node type '${nodeType}' failed!"
    }

  /**
   * Casts this source tree node to denote a source node of type [S], or `null` if the cast fails.
   */
  public inline fun <reified S : SourceNode> castOrNull(): SourceTreeNode<S>? =
    castOrNull(S::class.java)

  /**
   * Casts this source tree node to denote a source node of type [S].
   *
   * @throws IllegalArgumentException if the cast fails
   */
  public inline fun <reified S : SourceNode> cast(): SourceTreeNode<S> = cast(S::class.java)

  public companion object {
    /** Creates a new source tree node from the given [sourceFile]. */
    @JvmStatic
    public fun of(sourceFile: SourceFile): SourceTreeNode<SourceFile> =
      SourceTreeNode(sourceFile.path, sourceFile)

    /** Creates a new source tree node from the given [parentId] and [sourceEntity]. */
    @JvmStatic
    public fun <T : SourceEntity> of(
      parentId: QualifiedSourceNodeId<SourceContainer>,
      sourceEntity: T
    ): SourceTreeNode<T> {
      val qualifiedId =
        when (val entity = sourceEntity as SourceEntity) {
          is Type -> parentId.type(entity.name)
          is Variable -> parentId.variable(entity.name)
          is Function -> parentId.function(entity.signature)
        }.cast(sourceEntity.javaClass)
      return SourceTreeNode(qualifiedId, sourceEntity)
    }
  }
}

public val SourceTreeNode<SourceEntity>.parentId: QualifiedSourceNodeId<SourceContainer>
  get() = qualifiedId.parentId

/** Returns a source tree node that wraps the given [child] node and has [this] node as a parent. */
public fun <T : SourceEntity> SourceTreeNode<SourceContainer>.append(child: T): SourceTreeNode<T> =
  SourceTreeNode.of(qualifiedId, child)

/** Returns all the source tree nodes contained in [this] source file in top-down order. */
public fun SourceFile.walkSourceTree(): List<SourceTreeNode<*>> =
  SourceTreeNode.of(this).walkSourceTree()
