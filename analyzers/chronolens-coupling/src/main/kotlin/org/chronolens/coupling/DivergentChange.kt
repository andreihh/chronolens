/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import org.chronolens.core.analysis.Analyzer
import org.chronolens.core.analysis.AnalyzerSpec
import org.chronolens.core.analysis.OptionsProvider
import org.chronolens.core.analysis.Report
import org.chronolens.core.analysis.constrainTo
import org.chronolens.core.analysis.option
import org.chronolens.core.model.QualifiedSourceNodeId
import org.chronolens.core.model.Revision
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.RepositoryConnector.AccessMode
import org.chronolens.core.serialization.JsonModule
import org.chronolens.coupling.DivergentChangeReport.FileReport
import org.chronolens.coupling.Graph.Subgraph

internal class DivergentChangeAnalyzerSpec : AnalyzerSpec {
    override val name: String
        get() = "divergent-change"

    override val description: String
        get() =
            """Builds the temporal coupling graphs for the analyzed source files, detects the
            Divergent Change instances, reports the results to the standard output and dumps the
            coupling graphs for each source file in the '.chronolens/divergent-change' directory."""

    override fun create(optionsProvider: OptionsProvider): DivergentChangeAnalyzer =
        DivergentChangeAnalyzer(optionsProvider)
}

internal class DivergentChangeAnalyzer(optionsProvider: OptionsProvider) :
    Analyzer(optionsProvider) {

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

    private val minBlobDensity by
        option<Double>()
            .name("min-blob-density")
            .description("the minimum average degree (sum of coupling) of methods in a blob")
            .defaultValue(2.5)
            .constrainTo(min = 0.0)

    private val maxAntiCoupling by
        option<Double>()
            .name("max-anti-coupling")
            .description("the maximum degree (sum of coupling) of a method in an anti-blob")
            .defaultValue(0.5)
            .constrainTo(min = 0.0)

    private val minAntiBlobSize by
        option<Int>()
            .name("min-anti-blob-size")
            .description("the minimum size of an anti-blob")
            .defaultValue(10)
            .constrainTo(min = 1)

    private val minMetricValue by
        option<Int>()
            .name("min-metric-value")
            .description(
                "ignore sources that have fewer blobs / anti-blobs than the specified limit"
            )
            .defaultValue(0)
            .constrainTo(min = 0)

    private fun TemporalContext.aggregateGraphs(): List<Graph> {
        val idsByFile = ids.groupBy(QualifiedSourceNodeId<*>::sourcePath)
        // TODO: check if this could be simplified to:
        //   return idsByFile.map { (path, ids) -> buildGraphFrom(path.toString(), ids.toSet()) }
        return idsByFile.keys.map { path ->
            val ids = idsByFile[path].orEmpty().toSet()
            buildGraphFrom(path.toString(), ids)
        }
    }

    private fun analyze(history: Sequence<Revision>): DivergentChangeReport {
        val analyzer = HistoryAnalyzer(maxChangeSet, minRevisions, minCoupling)
        val temporalContext = analyzer.analyze(history)
        val graphs = temporalContext.aggregateGraphs()
        val coloredGraphs = mutableListOf<ColoredGraph>()
        val files = mutableListOf<FileReport>()
        for (graph in graphs) {
            val blobs = graph.findBlobs(minBlobDensity)
            val antiBlob = graph.findAntiBlob(maxAntiCoupling, minAntiBlobSize)
            coloredGraphs += graph.colorNodes(blobs, antiBlob)
            files += FileReport(graph.label, blobs, antiBlob)
        }
        files.sortByDescending(FileReport::value)
        return DivergentChangeReport(files, coloredGraphs)
    }

    override fun analyze(repository: Repository): Report {
        val report = analyze(repository.getHistory())
        val files = report.files.filter { it.value >= minMetricValue }
        // TODO: remove this part.
        val directory = File(".chronolens", "divergent-change")
        for (coloredGraph in report.coloredGraphs) {
            val graphDirectory = File(directory, coloredGraph.graph.label)
            graphDirectory.mkdirs()
            val graphFile = File(graphDirectory, "graph.json")
            graphFile.outputStream().use { out -> JsonModule.serialize(out, coloredGraph) }
        }
        return DivergentChangeReport(files, report.coloredGraphs)
    }
}

internal data class DivergentChangeReport(
    val files: List<FileReport>,
    val coloredGraphs: List<ColoredGraph>
) : Report {

    override fun toString(): String = JsonModule.stringify(this)

    data class FileReport(
        val file: String,
        val blobs: List<Subgraph>,
        val antiBlob: Subgraph?,
    ) {

        val responsibilities: Int = blobs.size + if (antiBlob != null) 1 else 0

        val category: String = "SOLID Breakers"
        val name: String = "Single Responsibility Breakers"
        val value: Int = responsibilities
    }
}

private fun Graph.colorNodes(blobs: List<Subgraph>, antiBlob: Subgraph?): ColoredGraph {
    val groups = blobs.map(Subgraph::nodes) + listOfNotNull(antiBlob?.nodes)
    return colorNodes(groups)
}
