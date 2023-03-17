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

package org.chronolens.coupling

import java.io.File
import org.chronolens.api.analysis.Analyzer
import org.chronolens.api.analysis.AnalyzerSpec
import org.chronolens.api.analysis.Option.Companion.constrainTo
import org.chronolens.api.analysis.OptionsProvider
import org.chronolens.api.analysis.Report
import org.chronolens.api.analysis.option
import org.chronolens.api.repository.Repository
import org.chronolens.api.repository.Repository.AccessMode
import org.chronolens.core.serialization.JsonModule
import org.chronolens.coupling.FeatureEnvyReport.FeatureEnvy
import org.chronolens.coupling.FeatureEnvyReport.FileReport
import org.chronolens.coupling.Graph.Node
import org.chronolens.model.QualifiedSourceNodeId
import org.chronolens.model.Revision
import org.chronolens.model.SourcePath
import org.chronolens.model.parseQualifiedSourceNodeIdFrom
import org.chronolens.model.qualifiedSourcePathOf

internal class FeatureEnvyAnalyzerSpec : AnalyzerSpec {
  override val name: String
    get() = "feature-envy"

  override val description: String
    get() =
      """Loads the persisted repository, builds the temporal coupling graphs for the analyzed
            source files, detects the Feature Envy instances, reports the results to the standard
            output and dumps the coupling graphs for each pair of source files in the
            '.chronolens/feature-envy' directory."""

  override fun create(optionsProvider: OptionsProvider): FeatureEnvyAnalyzer =
    FeatureEnvyAnalyzer(optionsProvider)
}

internal class FeatureEnvyAnalyzer(optionsProvider: OptionsProvider) : Analyzer(optionsProvider) {
  override val accessMode: AccessMode
    get() = AccessMode.ANY

  private val maxChangeSet by
    option<Int>()
      .name("max-change-set")
      .description("the maximum number of changed files in a revision")
      .defaultValue(100)
      .constrainTo(min = 1)

  private val minRevisions by
    option<Int>()
      .name("min-revisions")
      .description("the minimum number of revisions of a method or coupling relation")
      .defaultValue(5)
      .constrainTo(min = 1)

  private val minCoupling by
    option<Double>()
      .name("min-coupling")
      .description("the minimum temporal coupling between two methods")
      .defaultValue(0.1)
      .constrainTo(min = 0.0)

  private val minEnvyRatio by
    option<Double>()
      .name("min-envy-ratio")
      .description("the minimum ratio of coupling to another source file")
      .defaultValue(1.0)
      .constrainTo(min = 0.0, max = 1.0)

  private val maxEnviedFiles by
    option<Int>()
      .name("max-envied-files")
      .description("the maximum number of files envied by a method that will be reported")
      .defaultValue(1)
      .constrainTo(min = 1)

  private val minMetricValue by
    option<Int>()
      .name("min-metric-value")
      .description("ignore sources that have fewer Feature Envy instances than the specified limit")
      .defaultValue(1)
      .constrainTo(min = 0)

  private fun TemporalContext.buildColoredGraphs(
    featureEnvyInstancesByFile: Map<SourcePath, List<FeatureEnvy>>,
  ): List<ColoredGraph> {
    val idsByFile = ids.groupBy(QualifiedSourceNodeId<*>::sourcePath)
    val graphs =
      featureEnvyInstancesByFile.map { (path, instances) ->
        val enviedFiles = instances.map(FeatureEnvy::enviedFile).toSet()
        val nodeIds = (enviedFiles + path).map(idsByFile::getValue).flatten().toSet()
        buildGraphFrom(path.toString(), nodeIds)
      }
    return graphs.map { graph -> graph.colorNodes(featureEnvyInstancesByFile) }
  }

  private fun TemporalContext.computeFunctionToFileCouplings():
    SparseMatrix<QualifiedSourceNodeId<*>, Double> {
    val functionToFileCoupling = emptySparseHashMatrix<QualifiedSourceNodeId<*>, Double>()
    for ((id1, id2) in cells) {
      val function = id1
      val file = qualifiedSourcePathOf(id2.sourcePath)
      val coupling = coupling(id1, id2)
      functionToFileCoupling[function, file] =
        (functionToFileCoupling[function, file] ?: 0.0) + coupling
    }
    return functionToFileCoupling
  }

  private fun findFeatureEnvyInstances(
    functionToFileCoupling: SparseMatrix<QualifiedSourceNodeId<*>, Double>,
  ): List<FeatureEnvy> {
    val featureEnvyInstances = arrayListOf<FeatureEnvy>()
    for ((function, fileCouplings) in functionToFileCoupling) {
      fun couplingWithFile(f: QualifiedSourceNodeId<*>): Double = fileCouplings[f] ?: 0.0

      val file = qualifiedSourcePathOf(function.sourcePath)
      val selfCoupling = couplingWithFile(file)
      val couplingThreshold = selfCoupling * minEnvyRatio
      val enviedFiles =
        (fileCouplings - file).keys.sortedByDescending(::couplingWithFile).takeWhile { f ->
          couplingWithFile(f) > couplingThreshold
        }
      featureEnvyInstances +=
        enviedFiles.take(maxEnviedFiles).map { f ->
          FeatureEnvy(function, selfCoupling, f.sourcePath, couplingWithFile(f))
        }
    }
    return featureEnvyInstances.sortedByDescending(FeatureEnvy::enviedCoupling)
  }

  private fun analyze(history: Sequence<Revision>): FeatureEnvyReport {
    val analyzer = HistoryAnalyzer(maxChangeSet, minRevisions, minCoupling)
    val temporalContext = analyzer.analyze(history)
    val functionToFileCoupling = temporalContext.computeFunctionToFileCouplings()
    val featureEnvyInstancesByFile =
      findFeatureEnvyInstances(functionToFileCoupling).groupBy(FeatureEnvy::file)
    val fileReports =
      featureEnvyInstancesByFile
        .map { (file, instances) -> FileReport(file, instances) }
        .sortedByDescending(FileReport::featureEnvyCount)
    val coloredGraphs = temporalContext.buildColoredGraphs(featureEnvyInstancesByFile)
    return FeatureEnvyReport(fileReports, coloredGraphs)
  }

  override fun analyze(repository: Repository): Report {
    val report = analyze(repository.getHistory())
    val files = report.files.filter { it.value >= minMetricValue }
    // TODO: remove this part.
    val directory = File(".chronolens", "feature-envy")
    for (coloredGraph in report.coloredGraphs) {
      val graphDirectory = File(directory, coloredGraph.graph.label)
      graphDirectory.mkdirs()
      val graphFile = File(graphDirectory, "graph.json")
      graphFile.outputStream().use { out -> JsonModule.serialize(out, coloredGraph) }
    }
    return FeatureEnvyReport(files, report.coloredGraphs)
  }
}

internal data class FeatureEnvyReport(
  val files: List<FileReport>,
  val coloredGraphs: List<ColoredGraph>
) : Report {

  override val name: String
    get() = "feature-envy"

  data class FileReport(
    val file: SourcePath,
    val featureEnvyInstances: List<FeatureEnvy>,
  ) {

    val featureEnvyCount: Int = featureEnvyInstances.size

    val category: String = "Temporal Coupling Anti-Patterns"
    val name: String = "Feature Envy"
    val value: Int = featureEnvyCount
  }

  data class FeatureEnvy(
    val function: QualifiedSourceNodeId<*>,
    val coupling: Double,
    val enviedFile: SourcePath,
    val enviedCoupling: Double,
  ) {
    val file: SourcePath
      get() = function.sourcePath
  }
}

private fun Graph.colorNodes(
  featureEnvyInstancesByFile: Map<SourcePath, List<FeatureEnvy>>,
): ColoredGraph {
  val instances =
    featureEnvyInstancesByFile
      .getValue(SourcePath(label))
      .map(FeatureEnvy::function)
      .map(QualifiedSourceNodeId<*>::toString)
      .toSet()
  val fileGroups =
    nodes.map(Node::label).groupBy { parseQualifiedSourceNodeIdFrom(it).sourcePath }.values
  val groups = fileGroups + instances.map(::listOf)
  return colorNodes(groups)
}
