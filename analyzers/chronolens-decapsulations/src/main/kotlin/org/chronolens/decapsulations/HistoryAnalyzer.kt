/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.AddNode
import org.chronolens.core.model.EditFunction
import org.chronolens.core.model.EditVariable
import org.chronolens.core.model.Function
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.SourceTreeEdit.Companion.apply
import org.chronolens.core.model.Variable
import org.chronolens.core.model.sourcePath
import org.chronolens.core.model.walkSourceTree
import org.chronolens.core.repository.Transaction

internal class HistoryAnalyzer(private val ignoreConstants: Boolean) {
    private val sourceTree = SourceTree.empty()
    private val decapsulationsByField = hashMapOf<String, List<Decapsulation>>()

    private fun getField(nodeId: String): String? =
        DecapsulationAnalyzer.getField(sourceTree, nodeId)

    private fun getVisibility(nodeId: String): Int? =
        DecapsulationAnalyzer.getVisibility(sourceTree, nodeId)

    private fun isConstant(nodeId: String): Boolean =
        DecapsulationAnalyzer.isConstant(sourceTree, nodeId)

    private fun addDecapsulation(
        fieldId: String,
        nodeId: String,
        revisionId: String,
        message: String
    ) {
        val new = Decapsulation(fieldId, nodeId, revisionId, message)
        val current = decapsulationsByField[fieldId].orEmpty()
        decapsulationsByField[fieldId] = current + new
    }

    private fun visit(edit: AddNode<*>): Set<String> {
        val editedIds = hashSetOf<String>()
        for ((id, node) in edit.sourceTreeNode.walkSourceTree()) {
            if (node is Variable) {
                decapsulationsByField[id] = emptyList()
                editedIds += id
            } else if (node is Function) {
                editedIds += id
            }
        }
        return editedIds
    }

    private fun visit(edit: RemoveNode): Set<String> {
        val removedIds = hashSetOf<String>()
        for (node in sourceTree.walk(edit.id)) {
            decapsulationsByField -= node.qualifiedId
            removedIds += node.qualifiedId
        }
        return removedIds
    }

    private fun visit(transaction: Transaction): Set<String> {
        val editedIds = hashSetOf<String>()
        for (edit in transaction.edits) {
            when (edit) {
                is AddNode<*> -> editedIds += visit(edit)
                is RemoveNode -> editedIds -= visit(edit)
                is EditFunction -> editedIds += edit.id
                is EditVariable -> editedIds += edit.id
                else -> {}
            }
        }
        return editedIds
    }

    private fun getVisibility(ids: Set<String>): Map<String, Int> {
        val visibility = hashMapOf<String, Int>()
        for (id in ids) {
            visibility[id] = getVisibility(id) ?: continue
            val fieldId = getField(id) ?: continue
            visibility[fieldId] = getVisibility(fieldId) ?: continue
        }
        return visibility
    }

    private fun analyze(transaction: Transaction) {
        val editedIds = visit(transaction)
        val oldVisibility = getVisibility(editedIds)
        sourceTree.apply(transaction.edits)
        val newVisibility = getVisibility(editedIds)

        fun analyze(qualifiedId: String) {
            val fieldId = getField(qualifiedId) ?: return
            val fieldOld = oldVisibility[fieldId] ?: return
            val old = oldVisibility[qualifiedId]
            val new = newVisibility[qualifiedId] ?: return
            if (old == null && new > fieldOld) {
                addDecapsulation(
                    fieldId = fieldId,
                    nodeId = qualifiedId,
                    revisionId = transaction.revisionId,
                    message = "Added accessor with more relaxed visibility!"
                )
            } else if (old != null && new > old) {
                addDecapsulation(
                    fieldId = fieldId,
                    nodeId = qualifiedId,
                    revisionId = transaction.revisionId,
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

    private fun getDecapsulations(fieldId: String): List<Decapsulation> =
        if (ignoreConstants && isConstant(fieldId)) emptyList()
        else decapsulationsByField[fieldId].orEmpty()

    fun analyze(history: Sequence<Transaction>): Report {
        history.forEach(::analyze)
        val fieldsByFile = decapsulationsByField.keys.groupBy(String::sourcePath)
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

    data class FieldReport(val id: String, val decapsulations: List<Decapsulation>)
}
