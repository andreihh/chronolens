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
import org.chronolens.core.model.Function
import org.chronolens.core.model.ListEdit
import org.chronolens.core.model.Project
import org.chronolens.core.model.ProjectEdit
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceNode
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import org.chronolens.core.model.sourcePath
import org.chronolens.core.model.walkSourceTree
import org.chronolens.core.repository.Transaction
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import kotlin.math.ln
import kotlin.math.roundToInt

class HistoryAnalyzer(private val metric: Metric, private val skipDays: Int) {
    init {
        require(skipDays >= 0) { "Invalid number of skipped days '$skipDays'!" }
    }

    private val project = Project.empty()
    private val stats = hashMapOf<String, Stats>()

    private fun getSourcePath(id: String): String =
        project.get<SourceNode>(id).sourcePath

    private val List<ListEdit<String>>.churn: Int
        get() = filterIsInstance<ListEdit.Add<String>>().size

    private fun getChurn(edit: ProjectEdit): Int = when (edit) {
        is EditFunction -> edit.bodyEdits.churn
        is EditVariable -> edit.initializerEdits.churn
        else -> 0
    }

    private fun visit(edit: ProjectEdit, date: Instant) {
        when (edit) {
            is AddNode -> {
                for (node in edit.node.walkSourceTree()) {
                    stats[node.id] = Stats.create(date)
                }
            }
            is RemoveNode -> {
                val nodes = project.get<SourceNode>(edit.id).walkSourceTree()
                stats -= nodes.map(SourceNode::id)
            }
            else -> {
                val nodeStats = stats.getValue(edit.id)
                val day = nodeStats.creationDate.until(date, DAYS)
                stats[edit.id] =
                    if (day >= skipDays) nodeStats.updated(getChurn(edit))
                    else nodeStats.copy(revisions = nodeStats.revisions + 1)
            }
        }
        project.apply(edit)
    }

    private fun visit(transaction: Transaction) {
        for (edit in transaction.edits) {
            visit(edit, transaction.date)
        }
    }

    private fun getSize(node: SourceNode): Int = when (node) {
        is SourceFile -> node.entities.sumBy(::getSize)
        is Type -> node.members.sumBy(::getSize)
        is Function -> node.body.size
        is Variable -> node.initializer.size
    }

    private fun getMemberReport(id: String): MemberReport {
        val size = getSize(project.get<SourceNode>(id))
        val (_, revisions, changes, churn, absoluteDecay) = stats.getValue(id)
        val decay = absoluteDecay.roundToInt()
        val value = when (metric) {
            Metric.SIZE -> size
            Metric.REVISIONS -> revisions
            Metric.CHANGES -> changes
            Metric.CHURN -> churn
            Metric.DECAY -> decay
        }
        return MemberReport(id, size, revisions, changes, churn, decay, value)
    }

    fun analyze(history: Iterable<Transaction>): Report {
        history.forEach(::visit)
        val membersByFile = stats.keys.groupBy(::getSourcePath)
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

    enum class Metric {
        SIZE, REVISIONS, CHANGES, CHURN, DECAY
    }

    data class Report(val files: List<FileReport>)

    data class FileReport(val file: String, val members: List<MemberReport>) {
        val category: String = "SOLID Breakers"
        val name: String = "Open-Closed Breakers"
        val value: Int = members.sumBy(MemberReport::value)
    }

    data class MemberReport(
        val id: String,
        val size: Int,
        val revisions: Int,
        val changes: Int,
        val churn: Int,
        val decay: Int,
        val value: Int
    )
}

private data class Stats(
    val creationDate: Instant,
    val revisions: Int,
    val changes: Int,
    val churn: Int,
    val decay: Double
) {

    init {
        require(revisions > 0) { "Revisions must be positive '$revisions'!" }
        require(changes >= 0) { "Changes can't be negative '$changes'!" }
        require(changes <= revisions) {
            "Changes '$changes' can't be greater than revisions '$revisions'!"
        }
        require(churn >= 0) { "Churn can't be negative '$churn'!" }
        require(decay >= 0.0) { "Decay can't be negative '$decay'!" }
    }

    fun updated(addedChurn: Int): Stats = copy(
        revisions = revisions + 1,
        changes = changes + 1,
        churn = churn + addedChurn,
        decay = decay + ln(changes + 1.0) * addedChurn
    )

    companion object {
        @JvmStatic
        fun create(date: Instant): Stats = Stats(
            creationDate = date,
            revisions = 1,
            changes = 0,
            churn = 0,
            decay = 0.0
        )
    }
}
