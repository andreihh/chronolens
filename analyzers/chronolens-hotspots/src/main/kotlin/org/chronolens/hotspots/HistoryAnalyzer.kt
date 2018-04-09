/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.hotspots

import org.chronolens.core.model.AddNode
import org.chronolens.core.model.EditFunction
import org.chronolens.core.model.EditVariable
import org.chronolens.core.model.Project
import org.chronolens.core.model.ProjectEdit
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceNode
import org.chronolens.core.model.sourcePath
import org.chronolens.core.model.walkSourceTree
import org.chronolens.core.repository.Transaction
import kotlin.math.ln

class HistoryAnalyzer {
    private val project = Project.empty()
    private val changes = hashMapOf<String, Int>()
    private val churn = hashMapOf<String, Int>()
    private val weight = hashMapOf<String, Double>()

    private fun getSourcePath(id: String): String =
        project.get<SourceNode>(id).sourcePath

    private fun getChurn(edit: EditFunction): Int = edit.bodyEdits.size

    private fun getChurn(edit: EditVariable): Int = edit.initializerEdits.size

    private fun getChurn(edit: ProjectEdit): Int = when (edit) {
        is EditFunction -> getChurn(edit)
        is EditVariable -> getChurn(edit)
        else -> 0
    }

    private fun visit(edit: ProjectEdit) {
        when (edit) {
            is AddNode -> {
                for (node in edit.node.walkSourceTree()) {
                    changes[node.id] = 1
                    churn[node.id] = 0
                    weight[node.id] = 0.0
                }
            }
            is RemoveNode -> {
                val nodes = project.get<SourceNode>(edit.id).walkSourceTree()
                for (node in nodes) {
                    changes -= node.id
                    churn -= node.id
                    weight -= node.id
                }
            }
            else -> {
                val addedChurn = getChurn(edit)
                val scale = ln(1.0 * changes.getValue(edit.id))
                val addedWeight = scale * addedChurn
                changes[edit.id] = changes.getValue(edit.id) + 1
                churn[edit.id] = churn.getValue(edit.id) + addedChurn
                weight[edit.id] = weight.getValue(edit.id) + addedWeight
            }
        }
        project.apply(edit)
    }

    private fun visit(transaction: Transaction) {
        for (edit in transaction.edits) {
            visit(edit)
        }
    }

    private fun getMemberReport(id: String): MemberReport = MemberReport(
        id = id,
        revisions = changes.getValue(id),
        churn = churn.getValue(id),
        value = weight.getValue(id)
    )

    fun analyze(history: Iterable<Transaction>): Report {
        history.forEach(::visit)
        val membersByFile = changes.keys.groupBy(::getSourcePath)
        val sourcePaths = project.sources.map(SourceFile::path)
        val fileReports = sourcePaths.map { path ->
            val members = membersByFile[path].orEmpty()
            val memberReports = members
                .map(::getMemberReport)
                .sortedByDescending(MemberReport::value)
            FileReport(path, memberReports)
        }.sortedByDescending(FileReport::value)
        return Report(fileReports)
    }

    data class Report(val files: List<FileReport>)

    data class FileReport(val file: String, val members: List<MemberReport>) {
        val category: String = "SOLID Breakers"
        val name: String = "Open-Closed Breakers"
        val value: Double = members.sumByDouble(MemberReport::value)
    }

    data class MemberReport(
        val id: String,
        val revisions: Int,
        val churn: Int,
        val value: Double
    )
}
