/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import org.chronolens.core.cli.exit
import org.chronolens.core.repository.Transaction
import org.chronolens.core.serialization.JsonModule
import org.chronolens.coupling.Graph.Subgraph
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File

@Command(
    name = "divergent-change",
    description = [
        "Loads the persisted repository, builds the temporal coupling graphs " +
            "for the analyzed source files, detects the Divergent Change " +
            "instances, reports the results to the standard output and dumps " +
            "the coupling graphs for each source file in the " +
            "'.chronolens/divergent-change' directory."
    ],
    showDefaultValues = true
)
class DivergentChangeCommand : Subcommand() {
    override val name: String get() = "divergent-change"

    @Option(
        names = ["--max-change-set"],
        description = ["the maximum number of changed files in a revision"]
    )
    private var maxChangeSet: Int = 100

    @Option(
        names = ["--min-revisions"],
        description = [
            "the minimum number of revisions of a method or coupling relation"
        ]
    )
    private var minRevisions: Int = 5

    @Option(
        names = ["--min-coupling"],
        description = ["the minimum temporal coupling between two methods"]
    )
    private var minCoupling: Double = 0.1

    @Option(
        names = ["--min-blob-density"],
        description = [
            "the minimum average degree (sum of coupling) of methods in a blob"
        ]
    )
    private var minBlobDensity: Double = 2.5

    @Option(
        names = ["--max-anti-coupling"],
        description = [
            "the maximum degree (sum of coupling) of a method in an anti-blob"
        ]
    )
    private var maxAntiCoupling: Double = 0.5

    @Option(
        names = ["--min-anti-blob-size"],
        description = ["the minimum size of of an anti-blob"]
    )
    private var minAntiBlobSize: Int = 10

    @Option(
        names = ["--min-metric-value"],
        description = [
            "ignore source files that have less blobs / anti-blobs than the " +
                "specified limit"
        ]
    )
    private var minMetricValue: Int = 0

    private fun validateOptions() {
        if (maxChangeSet <= 0) exit("max-change-set must be positive!")
        if (minRevisions <= 0) exit("min-revisions must be positive!")
        if (minCoupling < 0.0) exit("min-coupling can't be negative!")
        if (minBlobDensity < 0.0) exit("min-blob-density can't be negative!")
        if (maxAntiCoupling < 0.0) exit("max-anti-coupling can't be negative!")
        if (minAntiBlobSize <= 0) exit("min-anti-blob-size must be positive!")
        if (minMetricValue < 0) exit("min-metric-value can't be negative!")
    }

    private fun analyze(history: Iterable<Transaction>): Report {
        val analyzer = HistoryAnalyzer(maxChangeSet, minRevisions, minCoupling)
        val graphs = analyzer.analyze(history).graphs.map { graph ->
            graph.filterNodes { (label, _) -> label.startsWith(graph.label) }
        }
        val coloredGraphs = mutableListOf<ColoredGraph>()
        val files = mutableListOf<FileReport>()
        for (graph in graphs) {
            val blobs = graph.findBlobs(minBlobDensity)
            val antiBlob = graph.findAntiBlob(maxAntiCoupling, minAntiBlobSize)
            coloredGraphs += graph.colorNodes(blobs, antiBlob)
            files += FileReport(graph.label, blobs, antiBlob)
        }
        files.sortByDescending(FileReport::value)
        return Report(files, coloredGraphs)
    }

    override fun run() {
        validateOptions()
        val repository = load()
        val report = analyze(repository.getHistory())
        val files = report.files.filter { it.value >= minMetricValue }
        JsonModule.serialize(System.out, files)
        val directory = File(".chronolens", "divergent-change")
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
        val coloredGraphs: List<ColoredGraph>
    )

    data class FileReport(
        val file: String,
        val blobs: List<Subgraph>,
        val antiBlob: Subgraph?
    ) {

        val responsibilities: Int = blobs.size + if (antiBlob != null) 1 else 0

        val category: String = "SOLID Breakers"
        val name: String = "Single Responsibility Breakers"
        val value: Int = responsibilities
    }
}

private fun Graph.colorNodes(
    blobs: List<Subgraph>,
    antiBlob: Subgraph?
): ColoredGraph {
    val groups = blobs.map(Subgraph::nodes) + listOfNotNull(antiBlob?.nodes)
    return colorNodes(groups)
}
