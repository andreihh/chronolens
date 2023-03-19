/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.model.ListEdit.Companion.apply
import org.chronolens.model.SetEdit.Companion.apply

/** An atomic change which should be applied to a [SourceTree]. */
public sealed class SourceTreeEdit {
  /** The qualified id of the edited node. */
  public abstract val qualifiedId: QualifiedSourceNodeId<*>

  /** The path of the [SourceFile] which contains the edited node. */
  public val sourcePath: SourcePath
    get() = qualifiedId.sourcePath

  /**
   * Applies this edit to the given [sourceTree].
   *
   * @throws IllegalStateException if the [sourceTree] has an invalid state and this edit couldn't
   * be applied
   */
  public fun applyOn(sourceTree: SourceTree) {
    sourceTree.mutate { sourceMap, nodeMap ->
      val sourcePath = sourcePath
      sourceMap -= sourcePath
      applyOn(nodeMap)
      val newSource = get(sourcePath)
      if (newSource != null) {
        sourceMap[sourcePath] = newSource
      }
    }
  }

  /**
   * Applies this edit on the given mutable map of [nodes].
   *
   * @throws IllegalStateException if the [nodes] have an invalid state and this edit couldn't be
   * applied
   */
  protected abstract fun applyOn(nodes: NodeHashMap)

  public companion object {
    /**
     * Applies the given [edit] to this source tree.
     *
     * @throws IllegalStateException if this source tree has an invalid state and the given [edit]
     * couldn't be applied
     */
    @JvmStatic
    public fun SourceTree.apply(edit: SourceTreeEdit) {
      edit.applyOn(this)
    }

    /** Utility method. */
    @JvmStatic
    public fun SourceTree.apply(edits: List<SourceTreeEdit>) {
      edits.forEach { apply(it) }
    }

    /** Utility method. */
    @JvmStatic
    public fun SourceTree.apply(vararg edits: SourceTreeEdit) {
      apply(edits.asList())
    }
  }
}

/**
 * Indicates that a [SourceNode] should be added to a source tree.
 *
 * @param T the type of the added source node
 * @property sourceNode the node which should be added to the source tree
 * @throws IllegalArgumentException if the [qualifiedId] and [sourceNode] kinds do not match
 */
public data class AddNode<out T : SourceNode>(
  override val qualifiedId: QualifiedSourceNodeId<T>,
  val sourceNode: T
) : SourceTreeEdit() {

  val sourceTreeNode: SourceTreeNode<T> = SourceTreeNode(qualifiedId, sourceNode)

  override fun applyOn(nodes: NodeHashMap) {
    check(qualifiedId !in nodes) { "Node '$qualifiedId' already exists!" }
    nodes.putSourceTree(sourceTreeNode)
    sourceTreeNode.castOrNull<SourceEntity>()?.let(nodes::updateAncestorsOf)
  }
}

/** Indicates that a [SourceNode] should be removed from a source tree. */
public data class RemoveNode(override val qualifiedId: QualifiedSourceNodeId<*>) :
  SourceTreeEdit() {
  override fun applyOn(nodes: NodeHashMap) {
    val node = nodes[qualifiedId] ?: error("Node '$qualifiedId' doesn't exist!")
    nodes.removeSourceTree(node)
    node.castOrNull<SourceEntity>()?.let(nodes::updateAncestorsOf)
  }
}

/**
 * Indicates that the properties of a [Type] within a [SourceTree] should be edited.
 *
 * @property supertypeEdits the edits which should be applied to the [Type.supertypes] of the type
 * with the given [qualifiedId]
 * @property modifierEdits the edits which should be applied to the [Type.modifiers] of the type
 * with the given [qualifiedId]
 */
public data class EditType(
  override val qualifiedId: QualifiedSourceNodeId<Type>,
  val supertypeEdits: List<SetEdit<Identifier>> = emptyList(),
  val modifierEdits: List<SetEdit<String>> = emptyList()
) : SourceTreeEdit() {

  override fun applyOn(nodes: NodeHashMap) {
    val type =
      nodes[qualifiedId]?.sourceNode as? Type? ?: error("Type '$qualifiedId' doesn't exist!")
    val newType =
      type.copy(
        supertypes = type.supertypes.apply(supertypeEdits),
        modifiers = type.modifiers.apply(modifierEdits)
      )
    val node = SourceTreeNode(qualifiedId, newType)
    nodes[qualifiedId] = node
    nodes.updateAncestorsOf(node)
  }
}

/**
 * Indicates that the properties of a [Function] within a [SourceTree] should be edited.
 *
 * @property parameterEdits the edits which should be applied to the [Function.parameters] of the
 * function with the given [qualifiedId]
 * @property modifierEdits the edits which should be applied to the [Function.modifiers] of the
 * function with the given [qualifiedId]
 * @property bodyEdits the edits which should be applied to the [Function.body] of the function with
 * the given [qualifiedId]
 */
public data class EditFunction(
  override val qualifiedId: QualifiedSourceNodeId<Function>,
  val parameterEdits: List<ListEdit<Identifier>> = emptyList(),
  val modifierEdits: List<SetEdit<String>> = emptyList(),
  val bodyEdits: List<ListEdit<String>> = emptyList()
) : SourceTreeEdit() {

  override fun applyOn(nodes: NodeHashMap) {
    val function =
      nodes[qualifiedId]?.sourceNode as? Function?
        ?: error("Function '$qualifiedId' doesn't exist!")
    val newFunction =
      function.copy(
        parameters = function.parameters.apply(parameterEdits),
        modifiers = function.modifiers.apply(modifierEdits),
        body = function.body.apply(bodyEdits)
      )
    val node = SourceTreeNode(qualifiedId, newFunction)
    nodes[qualifiedId] = node
    nodes.updateAncestorsOf(node)
  }
}

/**
 * Indicates that the properties of a [Variable] within a [SourceTree] should be edited.
 *
 * @property modifierEdits the edits which should be applied to the [Variable.modifiers] of the
 * variable with the given [qualifiedId]
 * @property initializerEdits the edits which should be applied to the [Variable.initializer] of the
 * variable with the given [qualifiedId]
 */
public data class EditVariable(
  override val qualifiedId: QualifiedSourceNodeId<Variable>,
  val modifierEdits: List<SetEdit<String>> = emptyList(),
  val initializerEdits: List<ListEdit<String>> = emptyList()
) : SourceTreeEdit() {

  override fun applyOn(nodes: NodeHashMap) {
    val variable =
      nodes[qualifiedId]?.sourceNode as? Variable?
        ?: error("Variable '$qualifiedId' doesn't exist!")
    val newVariable =
      variable.copy(
        modifiers = variable.modifiers.apply(modifierEdits),
        initializer = variable.initializer.apply(initializerEdits)
      )
    val node = SourceTreeNode(qualifiedId, newVariable)
    nodes[qualifiedId] = node
    nodes.updateAncestorsOf(node)
  }
}

/**
 * Updates all ancestors of the given modified [sourceTreeNode] from [this] mutable node map.
 *
 * @throws IllegalStateException if this node map has an invalid state and the ancestors couldn't be
 * updated
 */
private fun <T : SourceEntity> NodeHashMap.updateAncestorsOf(sourceTreeNode: SourceTreeNode<T>) {
  val (qualifiedId, entity) = sourceTreeNode

  fun Set<SourceEntity>.updatedWithEntity(): Set<SourceEntity> {
    val newEntities = LinkedHashSet<SourceEntity>(size)
    this.filterTo(newEntities) { it.kind != entity.kind || it.id != entity.id }
    if (qualifiedId in this@updateAncestorsOf) {
      newEntities += entity
    }
    return newEntities
  }

  fun SourceFile.updated() = copy(entities = entities.updatedWithEntity())
  fun Type.updated() = copy(members = members.updatedWithEntity())

  val parentId = sourceTreeNode.parentId
  when (val parent = this[parentId]?.sourceNode ?: error("Parent '$parentId' must exist!")) {
    is SourceFile -> {
      this[parentId] = SourceTreeNode(parentId, parent.updated())
    }
    is Type -> {
      val newParent = SourceTreeNode(parentId.cast(), parent.updated())
      this[parentId] = newParent
      updateAncestorsOf(newParent)
    }
    else -> error("Unknown container '${parent::kind}' with id '$parentId'!")
  }
}
