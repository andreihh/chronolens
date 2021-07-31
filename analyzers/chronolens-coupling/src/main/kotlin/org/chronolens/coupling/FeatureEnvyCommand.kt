/*
 * Copyright 2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.cli.Subcommand
import org.chronolens.core.cli.restrictTo
import org.chronolens.core.model.sourcePath
import org.chronolens.core.repository.Transaction
import org.chronolens.core.serialization.JsonModule
import org.chronolens.coupling.FeatureEnvyCommand.FeatureEnvy
import org.chronolens.coupling.Graph.Node
import java.io.File

internal class FeatureEnvyCommand : Subcommand() {
    override val help: String
        get() = """
        Loads the persisted repository, builds the temporal coupling graphs for
        the analyzed source files, detects the Feature Envy instances, reports
        the results to the standard output and dumps the coupling graphs for
        each pair of source files in the '.chronolens/feature-envy' directory.
    """
    private val maxChangeSet by option<Int>()
        .help("the maximum number of changed files in a revision")
        .defaultValue(100).restrictTo(min = 1)

    private val minRevisions by option<Int>().help(
        "the minimum number of revisions of a method or coupling relation"
    ).defaultValue(5).restrictTo(min = 1)

    private val minCoupling by option<Double>()
        .help("the minimum temporal coupling between two methods")
        .defaultValue(0.1).restrictTo(min = 0.0)

    private val minEnvyRatio by option<Double>()
        .help("the minimum ratio of coupling to another source file")
        .defaultValue(1.0).restrictTo(min = 0.0, max = 1.0)

    private val minMetricValue by option<Int>().help(
        """ignore source files that have less Feature Envy instances than the
        specified limit"""
    ).defaultValue(1).restrictTo(min = 0)

    private fun TemporalContext.buildColoredGraphs(
        featureEnvyInstancesByFile: Map<String, List<FeatureEnvy>>,
    ): List<ColoredGraph> {
        val idsByFile = ids.groupBy(String::sourcePath)
        val graphs = featureEnvyInstancesByFile.map { (path, instances) ->
            val enviedFiles = instances.map(FeatureEnvy::enviedGraph).toSet()
            val nodeIds =
                (enviedFiles + path).map(idsByFile::getValue).flatten().toSet()
            buildGraphFrom(path, nodeIds)
        }
        return graphs
            .map { graph -> graph.colorNodes(featureEnvyInstancesByFile) }
    }

    private fun TemporalContext.computeFunctionToFileCouplings():
        SparseMatrix<String, Double> {
            val functionToFileCoupling = emptySparseHashMatrix<String, Double>()
            for ((id1, id2) in cells) {
                val function = id1
                val file = id2.sourcePath
                val coupling = coupling(id1, id2)
                functionToFileCoupling[function, file] =
                    (functionToFileCoupling[function, file] ?: 0.0) + coupling
            }
            return functionToFileCoupling
        }

    private fun findFeatureEnvyInstances(
        functionToFileCoupling: SparseMatrix<String, Double>,
    ): List<FeatureEnvy> {
        val featureEnvyInstances = arrayListOf<FeatureEnvy>()
        for ((function, fileCouplings) in functionToFileCoupling) {
            val file = function.sourcePath
            val selfCoupling = fileCouplings[file] ?: 0.0
            val couplingThreshold = selfCoupling * minEnvyRatio
            val (enviedFile, enviedCoupling) =
                fileCouplings.maxByOrNull { it.value } ?: continue
            if (file != enviedFile && enviedCoupling > couplingThreshold) {
                featureEnvyInstances += FeatureEnvy(
                    function,
                    selfCoupling,
                    enviedFile,
                    enviedCoupling,
                )
            }
        }
        return featureEnvyInstances
            .sortedByDescending(FeatureEnvy::enviedCoupling)
    }

    private fun analyze(history: Sequence<Transaction>): Report {
        val analyzer = HistoryAnalyzer(maxChangeSet, minRevisions, minCoupling)
        val temporalContext = analyzer.analyze(history)
        val functionToFileCoupling =
            temporalContext.computeFunctionToFileCouplings()
        val featureEnvyInstancesByFile =
            findFeatureEnvyInstances(functionToFileCoupling)
                .groupBy(FeatureEnvy::file)
        val fileReports = featureEnvyInstancesByFile
            .map { (file, instances) -> FileReport(file, instances) }
            .sortedByDescending(FileReport::featureEnvyCount)
        val coloredGraphs =
            temporalContext.buildColoredGraphs(featureEnvyInstancesByFile)
        return Report(fileReports, coloredGraphs)
    }

    override fun run() {
        val repository = load()
        val report = analyze(repository.getHistory())
        val files = report.files.filter { it.value >= minMetricValue }
        JsonModule.serialize(System.out, files)
        val directory = File(".chronolens", "feature-envy")
        for (coloredGraph in report.coloredGraphs) {
            val graphDirectory = File(directory, coloredGraph.graph.label)
            graphDirectory.mkdirs()
            val graphFile = File(graphDirectory, "graph.json")
            graphFile.outputStream().use { out ->
                JsonModule.serialize(out, coloredGraph)
            }
        }
    }

    data class Report(
        val files: List<FileReport>,
        val coloredGraphs: List<ColoredGraph>,
    )

    data class FileReport(
        val file: String,
        val featureEnvyInstances: List<FeatureEnvy>,
    ) {

        val featureEnvyCount: Int = featureEnvyInstances.size

        val category: String = "Temporal Coupling Anti-Patterns"
        val name: String = "Feature Envy"
        val value: Int = featureEnvyCount
    }

    data class FeatureEnvy(
        val node: String,
        val coupling: Double,
        val enviedGraph: String,
        val enviedCoupling: Double,
    ) {
        val file: String get() = node.sourcePath
    }
}

private fun Graph.colorNodes(
    featureEnvyInstancesByFile: Map<String, List<FeatureEnvy>>,
): ColoredGraph {
    val instances =
        featureEnvyInstancesByFile.getValue(label).map(FeatureEnvy::node)
    val fileGroups = nodes.map(Node::label).groupBy { it.sourcePath }.values
    val groups = fileGroups + instances.map(::listOf)
    return colorNodes(groups)
}
