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

package org.chronolens.decapsulations

import org.chronolens.core.analysis.Analyzer
import org.chronolens.core.analysis.AnalyzerSpec
import org.chronolens.core.analysis.OptionsProvider
import org.chronolens.core.analysis.Report
import org.chronolens.core.analysis.constrainTo
import org.chronolens.core.analysis.option
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.RepositoryConnector.AccessMode
import org.chronolens.core.serialization.JsonModule
import org.chronolens.decapsulations.HistoryAnalyzer.FileReport

internal class DecapsulationAnalyzerSpec : AnalyzerSpec {
    override val name: String
        get() = "decapsulations"

    override val description: String
        get() =
            """Detects the decapsulations that occurred during the evolution of the repository and
            reports the results to the standard output."""

    override fun create(optionsProvider: OptionsProvider): DecapsulationAnalyzer =
        DecapsulationAnalyzer(optionsProvider)
}

internal class DecapsulationAnalyzer(optionsProvider: OptionsProvider) : Analyzer(optionsProvider) {
    override val accessMode: AccessMode
        get() = AccessMode.ANY

    private val keepConstants by
        option<Boolean>()
            .name("keep-constants")
            .description("do not ignore decapsulations of constant fields")
            .default(false)

    private val minMetricValue by
        option<Int>()
            .name("min-metric-value")
            .description("ignore sources that have less decapsulations than the specified limit")
            .default(0)
            .constrainTo(min = 0)

    override fun analyze(repository: Repository): DecapsulationReport {
        val report = HistoryAnalyzer(!keepConstants).analyze(repository.getHistory())
        val files = report.files.filter { it.value >= minMetricValue }
        return DecapsulationReport(files)
    }
}

internal data class DecapsulationReport(val files: List<FileReport>) : Report {
    override fun toString(): String = JsonModule.stringify(this)
}
