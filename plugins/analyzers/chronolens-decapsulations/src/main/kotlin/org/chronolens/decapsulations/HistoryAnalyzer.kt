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

package org.chronolens.decapsulations

import org.chronolens.model.AddNode
import org.chronolens.model.EditFunction
import org.chronolens.model.EditVariable
import org.chronolens.model.Function
import org.chronolens.model.QualifiedSourceNodeId
import org.chronolens.model.RemoveNode
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId
import org.chronolens.model.SourceFile
import org.chronolens.model.SourceNodeKind.FUNCTION
import org.chronolens.model.SourceNodeKind.VARIABLE
import org.chronolens.model.SourcePath
import org.chronolens.model.SourceTree
import org.chronolens.model.SourceTreeEdit.Companion.apply
import org.chronolens.model.Variable
import org.chronolens.model.cast

internal class HistoryAnalyzer(private val ignoreConstants: Boolean) {
  private val sourceTree = SourceTree.empty()
  private val decapsulationsByField =
    hashMapOf<QualifiedSourceNodeId<Variable>, List<Decapsulation>>()

  private fun getField(nodeId: QualifiedSourceNodeId<*>): QualifiedSourceNodeId<Variable>? =
    AbstractDecapsulationAnalyzer.getField(sourceTree, nodeId)

  private fun getVisibility(nodeId: QualifiedSourceNodeId<*>): Int? =
    AbstractDecapsulationAnalyzer.getVisibility(sourceTree, nodeId)

  private fun isConstant(nodeId: QualifiedSourceNodeId<*>): Boolean =
    AbstractDecapsulationAnalyzer.isConstant(sourceTree, nodeId)

  private fun addDecapsulation(
    fieldId: QualifiedSourceNodeId<Variable>,
    nodeId: QualifiedSourceNodeId<*>,
    revisionId: RevisionId,
    message: String
  ) {
    val new = Decapsulation(fieldId, nodeId, revisionId, message)
    val current = decapsulationsByField[fieldId].orEmpty()
    decapsulationsByField[fieldId] = current + new
  }

  private fun visit(edit: AddNode<*>): Set<QualifiedSourceNodeId<*>> {
    val editedIds = hashSetOf<QualifiedSourceNodeId<*>>()
    for ((id, node) in edit.sourceTreeNode.walkSourceTree()) {
      if (node.kind == VARIABLE) {
        decapsulationsByField[id.cast()] = emptyList()
      }
      if (node.kind == VARIABLE || node.kind == FUNCTION) {
        editedIds += id
      }
    }
    return editedIds
  }

  private fun visit(edit: RemoveNode): Set<QualifiedSourceNodeId<*>> {
    val removedIds = hashSetOf<QualifiedSourceNodeId<*>>()
    for (node in sourceTree.walk(edit.qualifiedId)) {
      if (node.kind == VARIABLE) {
        decapsulationsByField -= node.qualifiedId.cast()
      }
      removedIds += node.qualifiedId
    }
    return removedIds
  }

  private fun visit(revision: Revision): Set<QualifiedSourceNodeId<*>> {
    val editedIds = hashSetOf<QualifiedSourceNodeId<*>>()
    for (edit in revision.edits) {
      when (edit) {
        is AddNode<*> -> editedIds += visit(edit)
        is RemoveNode -> editedIds -= visit(edit)
        is EditFunction -> editedIds += edit.qualifiedId
        is EditVariable -> editedIds += edit.qualifiedId
        else -> {}
      }
    }
    return editedIds
  }

  private fun getVisibility(
    ids: Set<QualifiedSourceNodeId<*>>
  ): Map<QualifiedSourceNodeId<*>, Int> {
    val visibility = hashMapOf<QualifiedSourceNodeId<*>, Int>()
    for (id in ids) {
      visibility[id] = getVisibility(id) ?: continue
      val fieldId = getField(id) ?: continue
      visibility[fieldId] = getVisibility(fieldId) ?: continue
    }
    return visibility
  }

  private fun analyze(revision: Revision) {
    val editedIds = visit(revision)
    val oldVisibility = getVisibility(editedIds)
    sourceTree.apply(revision.edits)
    val newVisibility = getVisibility(editedIds)

    fun analyze(qualifiedId: QualifiedSourceNodeId<*>) {
      val fieldId = getField(qualifiedId) ?: return
      val fieldOld = oldVisibility[fieldId] ?: return
      val old = oldVisibility[qualifiedId]
      val new = newVisibility[qualifiedId] ?: return
      if (old == null && new > fieldOld) {
        addDecapsulation(
          fieldId = fieldId,
          nodeId = qualifiedId,
          revisionId = revision.id,
          message = "Added accessor with more relaxed visibility!"
        )
      } else if (old != null && new > old) {
        addDecapsulation(
          fieldId = fieldId,
          nodeId = qualifiedId,
          revisionId = revision.id,
          message = "Relaxed accessor visibility!"
        )
      }
    }

    for (id in editedIds) {
      val node = sourceTree[id]
      if (node is Variable || node is Function) {
        analyze(id)
      }
    }
  }

  private fun getDecapsulations(fieldId: QualifiedSourceNodeId<Variable>): List<Decapsulation> =
    if (ignoreConstants && isConstant(fieldId)) emptyList()
    else decapsulationsByField[fieldId].orEmpty()

  fun analyze(history: Sequence<Revision>): Report {
    history.forEach(::analyze)
    val fieldsByFile = decapsulationsByField.keys.groupBy(QualifiedSourceNodeId<*>::sourcePath)
    val sourcePaths = sourceTree.sources.map(SourceFile::path)
    val fileReports =
      sourcePaths
        .map { path ->
          val fields = fieldsByFile[path].orEmpty()
          val fieldReports =
            fields
              .map { id -> FieldReport(id, getDecapsulations(id)) }
              .sortedByDescending { it.decapsulations.size }
          FileReport(path, fieldReports)
        }
        .sortedByDescending(FileReport::value)
    return Report(fileReports)
  }

  data class Report(val files: List<FileReport>)

  data class FileReport(val file: SourcePath, val fields: List<FieldReport>) {
    val decapsulations: Int = fields.sumOf { it.decapsulations.size }

    val category: String = "SOLID Breakers"
    val name: String = "Encapsulation Breakers"
    val value: Int = decapsulations
  }

  data class FieldReport(
    val id: QualifiedSourceNodeId<Variable>,
    val decapsulations: List<Decapsulation>
  )
}
