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
import org.chronolens.coupling.Graph.Node
import org.chronolens.coupling.Graph.Subgraph
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
        .help("the minimum ratio of coupling to another graph")
        .defaultValue(1.0).restrictTo(min = 0.0, max = 1.0)

    private val minMetricValue by option<Int>().help(
        """ignore source files that have less Feature Envy instances than the
        specified limit"""
    ).defaultValue(1).restrictTo(min = 0)

    private fun findFeatureEnvyInstances(
        graphs: List<Graph>,
        nodeToGraphCoupling: CouplingMap<String, Double>,
    ): List<FeatureEnvy> {
        fun getCoupling(node: String, graph: String): Double =
            nodeToGraphCoupling[node, graph] ?: 0.0

        val featureEnvyInstances = mutableListOf<FeatureEnvy>()
        for ((graph, graphNodes, _) in graphs) {
            for ((node, _) in graphNodes) {
                if (!node.startsWith(graph)) continue
                val selfCoupling = getCoupling(node, graph)
                var maxCouplingGraph: String? = null
                var maxCoupling = 0.0
                for ((otherGraph, _, _) in graphs) {
                    if (otherGraph == graph) continue
                    val otherCoupling = getCoupling(node, otherGraph)
                    if (maxCoupling < otherCoupling) {
                        maxCouplingGraph = otherGraph
                        maxCoupling = otherCoupling
                    }
                }
                maxCouplingGraph ?: continue
                if (maxCoupling > selfCoupling * minEnvyRatio) {
                    featureEnvyInstances += FeatureEnvy(
                        node = node,
                        graph = graph,
                        coupling = selfCoupling,
                        enviedGraph = maxCouplingGraph,
                        enviedCoupling = maxCoupling,
                    )
                }
            }
        }
        return featureEnvyInstances
            .sortedByDescending(FeatureEnvy::enviedCoupling)
    }

    private fun buildColoredGraphs(
        graphs: List<Graph>,
        featureEnvyInstances: List<FeatureEnvy>,
    ): List<ColoredGraph> {
        val graphsByLabel = graphs.associateBy(Graph::label)
        val featureEnvyNodes =
            featureEnvyInstances.map(FeatureEnvy::node).toSet()
        val graphPairs =
            featureEnvyInstances.map { (_, graph, _, enviedGraph, _) ->
                if (graph < enviedGraph) graph to enviedGraph
                else enviedGraph to graph
            }.toSet()
        return graphPairs.map { (g1Label, g2Label) ->
            val g1 = graphsByLabel.getValue(g1Label)
            val g2 = graphsByLabel.getValue(g2Label)
            val mergedGraph = g1.mergeWith(g2)
            val groups = listOf(
                g1.nodes.map(Node::label) - featureEnvyNodes,
                g2.nodes.map(Node::label) - featureEnvyNodes,
            ) + mergedGraph.nodes.map(Node::label)
                .filter(featureEnvyNodes::contains)
                .map(::listOf)
            mergedGraph.colorNodes(groups)
        }
    }

    private fun groupFeatureEnvyInstancesByFile(
        featureEnvyInstances: List<FeatureEnvy>
    ): List<FileReport> = featureEnvyInstances
        .groupBy(FeatureEnvy::graph)
        .map { (file, featureEnvyInstances) ->
            FileReport(file, featureEnvyInstances)
        }

    private fun analyze(history: Sequence<Transaction>): Report {
        val analyzer = HistoryAnalyzer(maxChangeSet, minRevisions, minCoupling)
        val graphs = analyzer.analyze(history).graphs
        val nodeToGraphCoupling = emptyCouplingHashMap<String, Double>()
        for (graph in graphs) {
            for (edge in graph.edges) {
                val (id1, id2, _, coupling) = edge
                nodeToGraphCoupling[id1, id2.sourcePath] =
                    (nodeToGraphCoupling[id1, id2.sourcePath] ?: 0.0) + coupling
            }
        }
        val featureEnvyInstances =
            findFeatureEnvyInstances(graphs, nodeToGraphCoupling)
        val fileReports = groupFeatureEnvyInstancesByFile(featureEnvyInstances)
        val coloredGraphs = buildColoredGraphs(graphs, featureEnvyInstances)
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
        val graph: String,
        val coupling: Double,
        val enviedGraph: String,
        val enviedCoupling: Double,
    )
}
