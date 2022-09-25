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

package org.chronolens.churn

import org.chronolens.churn.HistoryAnalyzer.FileReport
import org.chronolens.churn.HistoryAnalyzer.Metric
import org.chronolens.core.analysis.Analyzer
import org.chronolens.core.analysis.AnalyzerSpec
import org.chronolens.core.analysis.Option.Companion.constrainTo
import org.chronolens.core.analysis.OptionsProvider
import org.chronolens.core.analysis.OptionsProvider.Companion.option
import org.chronolens.core.analysis.Report
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.RepositoryConnector.AccessMode
import org.chronolens.core.serialization.JsonModule

internal class ChurnAnalyzerSpec : AnalyzerSpec {
    override val name: String
        get() = "churn"

    override val description: String
        get() =
            """Detects the churners in the repository and reports the results to the standard
            output."""

    override fun create(optionsProvider: OptionsProvider): ChurnAnalyzer =
        ChurnAnalyzer(optionsProvider)
}

internal class ChurnAnalyzer(optionsProvider: OptionsProvider) : Analyzer(optionsProvider) {
    override val accessMode: AccessMode
        get() = AccessMode.ANY

    private val metric by
        option<Metric>()
            .name("metric")
            .description("the metric used to rank and highlight source nodes")
            .defaultValue(Metric.WEIGHTED_CHURN)

    private val skipDays by
        option<Int>()
            .name("skip-days")
            .description(
                """when analyzing source nodes, ignore revisions occurring in the first specified
                number of days, counting from the revision when the source node was created."""
            )
            .defaultValue(14)
            .constrainTo(min = 0)

    private val minMetricValue by
        option<Int>()
            .name("min-metric-value")
            .description("ignore sources that have less churn than the specified limit")
            .defaultValue(0)
            .constrainTo(min = 0)

    override fun analyze(repository: Repository): ChurnReport {
        val report = HistoryAnalyzer(metric, skipDays).analyze(repository.getHistory())
        val files = report.files.filter { it.value >= minMetricValue }
        return ChurnReport(files)
    }
}

internal data class ChurnReport(val files: List<FileReport>) : Report {
    override fun toString(): String = JsonModule.stringify(this)
}
