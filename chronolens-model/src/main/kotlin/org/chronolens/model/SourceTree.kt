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

import java.util.Collections.unmodifiableCollection

/**
 * A snapshot of a source tree at a specific point in time.
 *
 * It indexes all contained source nodes to allow fast access by qualified id.
 */
public class SourceTree
private constructor(
  private val sourceMap: HashMap<SourcePath, SourceFile>,
  private val nodeMap: NodeHashMap
) {

  /** The source files in this source tree. */
  public val sources: Collection<SourceFile>
    get() = unmodifiableCollection(sourceMap.values)

  /** Returns the node with the specified [id], or `null` if no such node was found. */
  public operator fun get(id: QualifiedSourceNodeId<*>): SourceNode? = nodeMap[id]?.sourceNode

  /** Returns the source file with the specified [path], or `null` if no such file was found. */
  public operator fun get(path: SourcePath): SourceFile? = getOrNull(qualifiedSourcePathOf(path))

  /** Returns whether the specified [id] exists in this source tree. */
  public operator fun contains(id: QualifiedSourceNodeId<*>): Boolean = get(id) != null

  /**
   * Returns the node of type [T] with the specified [id], or `null` if no such node was found.
   *
   * @throws IllegalStateException if the requested node is not of type [T]
   */
  public inline fun <reified T : SourceNode> getOrNull(id: QualifiedSourceNodeId<T>): T? {
    val node = get(id) ?: return null
    check(node is T) { "'$id' is not of type '${T::class}'!" }
    return node
  }

  /**
   * Returns the node of type [T] with the specified [id].
   *
   * @throws IllegalStateException if the requested node is not of type [T], or if no such node was
   * found
   */
  @JvmName("getNode")
  public inline fun <reified T : SourceNode> get(id: QualifiedSourceNodeId<T>): T =
    checkNotNull(getOrNull(id)) { "Node '$id' not found!" }

  /** Returns all the source nodes in this source tree in top-down order. */
  public fun walk(): Iterable<SourceTreeNode<*>> = sources.flatMap(SourceFile::walkSourceTree)

  /**
   * Returns all the source nodes in the subtree rooted at the given [id] in top-down order.
   *
   * @throws IllegalStateException if a node with the given [id] doesn't exist
   */
  public fun walk(id: QualifiedSourceNodeId<*>): Iterable<SourceTreeNode<*>> =
    nodeMap[id]?.walkSourceTree() ?: error("Source node '$id' doesn't exist in the source tree!")

  internal fun mutate(
    block: SourceTree.(sourceMap: HashMap<SourcePath, SourceFile>, nodeMap: NodeHashMap) -> Unit
  ) {
    this.block(sourceMap, nodeMap)
  }

  public companion object {
    /**
     * Creates and returns a source tree from the given [sources].
     *
     * @throws IllegalArgumentException if the given [sources] contain duplicate ids
     */
    @JvmStatic
    public fun of(sources: Collection<SourceFile>): SourceTree {
      val sourceMap = HashMap<SourcePath, SourceFile>(sources.size)
      for (source in sources) {
        require(source.path !in sourceMap) {
          "Source tree contains duplicate source '${source.path}'!"
        }
        sourceMap[source.path] = source
      }
      val nodeMap = NodeHashMap()
      sources.forEach(nodeMap::putSourceTree)
      return SourceTree(sourceMap, nodeMap)
    }

    /** Creates and returns an empty source tree. */
    @JvmStatic public fun empty(): SourceTree = SourceTree(HashMap(0), NodeHashMap(0))
  }
}

/** A hash map from qualified ids to source tree nodes. */
internal typealias NodeHashMap = HashMap<QualifiedSourceNodeId<*>, SourceTreeNode<*>>

internal fun NodeHashMap.putSourceTree(root: SourceTreeNode<*>) {
  this += root.walkSourceTree().associateBy(SourceTreeNode<*>::qualifiedId)
}

internal fun NodeHashMap.putSourceTree(root: SourceFile) = putSourceTree(SourceTreeNode.of(root))

internal fun NodeHashMap.removeSourceTree(root: SourceTreeNode<*>) {
  this -= root.walkSourceTree().map(SourceTreeNode<*>::qualifiedId).toSet()
}
