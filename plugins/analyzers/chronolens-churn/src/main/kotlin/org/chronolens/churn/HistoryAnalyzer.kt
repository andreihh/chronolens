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

package org.chronolens.churn

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import kotlin.math.ln
import kotlin.math.roundToInt
import org.chronolens.model.AddNode
import org.chronolens.model.EditFunction
import org.chronolens.model.EditVariable
import org.chronolens.model.Function
import org.chronolens.model.ListEdit
import org.chronolens.model.QualifiedSourceNodeId
import org.chronolens.model.RemoveNode
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId
import org.chronolens.model.SourceFile
import org.chronolens.model.SourceNode
import org.chronolens.model.SourcePath
import org.chronolens.model.SourceTree
import org.chronolens.model.SourceTreeEdit
import org.chronolens.model.SourceTreeEdit.Companion.apply
import org.chronolens.model.SourceTreeNode
import org.chronolens.model.Type
import org.chronolens.model.Variable

internal class HistoryAnalyzer(private val metric: Metric, private val skipDays: Int) {

  init {
    require(skipDays >= 0) { "Invalid number of skipped days '$skipDays'!" }
  }

  private val sourceTree = SourceTree.empty()
  private val stats = hashMapOf<QualifiedSourceNodeId<*>, Stats>()

  private fun updateStats(
    id: QualifiedSourceNodeId<*>,
    revisionId: RevisionId,
    date: Instant,
    churn: Int
  ) {
    val nodeStats = stats.getValue(id)
    val day = nodeStats.creationDate.until(date, DAYS)
    stats[id] =
      if (day < skipDays) nodeStats.updated(revisionId) else nodeStats.updated(revisionId, churn)
  }

  private fun visit(revisionId: RevisionId, date: Instant, edit: SourceTreeEdit) {
    when (edit) {
      is AddNode<*> -> {
        stats +=
          edit.sourceTreeNode
            .walkSourceTree()
            .filter { (_, node) -> node.isMember }
            .associate { it.qualifiedId to Stats.create(revisionId, date) }
      }
      is RemoveNode -> {
        stats -=
          sourceTree
            .walk(edit.qualifiedId)
            .filter { (_, node) -> node.isMember }
            .map(SourceTreeNode<*>::qualifiedId)
            .toSet()
      }
      is EditFunction -> updateStats(edit.qualifiedId, revisionId, date, edit.churn)
      is EditVariable -> updateStats(edit.qualifiedId, revisionId, date, edit.churn)
      else -> {}
    }
    sourceTree.apply(edit)
  }

  private fun visit(revision: Revision) {
    for (edit in revision.edits) {
      visit(revision.id, revision.date, edit)
    }
  }

  private fun getMemberReport(id: QualifiedSourceNodeId<*>): MemberReport {
    val size = getSize(sourceTree.get<SourceNode>(id))
    val (_, revisions, changes, churn, weightedChurn) = stats.getValue(id)
    return MemberReport(
      id = id,
      size = size,
      revisions = revisions.size,
      changes = changes.size,
      churn = churn,
      weightedChurn = weightedChurn
    )
  }

  private fun getMemberValue(member: MemberReport): Int =
    when (metric) {
      Metric.SIZE -> member.size
      Metric.REVISIONS -> member.revisions
      Metric.CHANGES -> member.changes
      Metric.CHURN -> member.churn
      Metric.WEIGHTED_CHURN -> member.weightedChurn.roundToInt()
    }

  fun analyze(history: Sequence<Revision>): Report {
    history.forEach(::visit)
    val membersByFile = stats.keys.groupBy(QualifiedSourceNodeId<*>::sourcePath)
    val sourcePaths = sourceTree.sources.map(SourceFile::path)
    val fileReports =
      sourcePaths
        .map { path ->
          val members = membersByFile[path].orEmpty()
          val memberStats = members.map(stats::getValue)
          val memberReports = members.map(::getMemberReport).sortedByDescending(::getMemberValue)
          val revisions = memberStats.flatMap(Stats::revisions).distinct().size
          val changes = memberStats.flatMap(Stats::changes).distinct().size
          FileReport(path, metric, memberReports, revisions, changes)
        }
        .sortedByDescending(FileReport::value)
    return Report(fileReports)
  }

  enum class Metric {
    SIZE,
    REVISIONS,
    CHANGES,
    CHURN,
    WEIGHTED_CHURN
  }

  data class Report(val files: List<FileReport>)

  data class FileReport(
    val file: SourcePath,
    val metric: Metric,
    val members: List<MemberReport>,
    val revisions: Int,
    val changes: Int
  ) {

    val size: Int = members.sumOf(MemberReport::size)
    val churn: Int = members.sumOf(MemberReport::churn)
    val weightedChurn: Double = members.sumOf(MemberReport::weightedChurn)

    val category: String = "SOLID Breakers"
    val name: String = "Open-Closed Breakers"
    val value: Int =
      when (metric) {
        Metric.SIZE -> size
        Metric.REVISIONS -> revisions
        Metric.CHANGES -> changes
        Metric.CHURN -> churn
        Metric.WEIGHTED_CHURN -> weightedChurn.roundToInt()
      }
  }

  data class MemberReport(
    val id: QualifiedSourceNodeId<*>,
    val size: Int,
    val revisions: Int,
    val changes: Int,
    val churn: Int,
    val weightedChurn: Double
  )
}

private data class Stats(
  val creationDate: Instant,
  val revisions: Set<RevisionId>,
  val changes: Set<RevisionId>,
  val churn: Int,
  val weightedChurn: Double
) {

  init {
    require(revisions.containsAll(changes)) {
      "Changes '$changes' must be a subset of revisions '$revisions'!"
    }
    require(churn >= 0) { "Churn can't be negative '$churn'!" }
    require(weightedChurn >= 0.0) { "Weighted churn can't be negative '$weightedChurn'!" }
  }

  fun updated(revisionId: RevisionId): Stats = copy(revisions = revisions + revisionId)

  fun updated(revisionId: RevisionId, addedChurn: Int): Stats =
    copy(
      revisions = revisions + revisionId,
      changes = changes + revisionId,
      churn = churn + addedChurn,
      weightedChurn = weightedChurn + ln(changes.size + 1.0) * addedChurn
    )

  companion object {
    @JvmStatic
    fun create(revisionId: RevisionId, date: Instant): Stats =
      Stats(
        creationDate = date,
        revisions = setOf(revisionId),
        changes = emptySet(),
        churn = 0,
        weightedChurn = 0.0
      )
  }
}

private val SourceNode.isMember: Boolean
  get() = this is Function || this is Variable

private val List<ListEdit<String>>.churn: Int
  get() = filterIsInstance<ListEdit.Add<String>>().size

private val EditFunction.churn: Int
  get() = bodyEdits.churn
private val EditVariable.churn: Int
  get() = initializerEdits.churn

private fun getSize(node: SourceNode): Int =
  when (node) {
    is SourceFile -> node.entities.sumOf(::getSize)
    is Type -> node.members.sumOf(::getSize)
    is Function -> node.body.size
    is Variable -> node.initializer.size
  }

private fun List<Set<String>>.union(): Set<String> =
  if (isEmpty()) emptySet() else reduce { acc, set -> acc.union(set) }
