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

package org.chronolens.churn

import org.chronolens.churn.HistoryAnalyzer.Metric
import org.chronolens.core.cli.Subcommand
import org.chronolens.core.cli.restrictTo
import org.chronolens.core.serialization.JsonModule

class ChurnersCommand : Subcommand() {
    override val help: String get() = """
        Loads the persisted repository, detects the churners of the system and
        reports the results to the standard output.
    """

    private val metric by option<Metric>()
        .help("the metric used to rank and highlight source nodes")
        .defaultValue(Metric.WEIGHTED_CHURN)

    private val skipDays by option<Int>().help("""
        when analyzing source nodes, ignore revisions occurring in the
        first specified number of days, counting from the revision
        when the source node was created.
    """).defaultValue(14).restrictTo(min = 0)

    private val minMetricValue by option<Int>().help("""
        ignore source files that have less churn than the specified limit
    """).defaultValue(0).restrictTo(min = 0)

    override fun execute() {
        val analyzer = HistoryAnalyzer(metric, skipDays)
        val repository = load()
        val report = analyzer.analyze(repository.getHistory())
        val files = report.files.filter { it.value >= minMetricValue }
        JsonModule.serialize(System.out, files)
    }
}
