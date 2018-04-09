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

package org.chronolens.decapsulations

import org.chronolens.core.cli.Subcommand
import org.chronolens.core.serialization.JsonModule
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
    name = "decapsulations",
    description = [
        "Loads the persisted repository, detects the decapsulations that " +
            "occurred during the evolution of the project and reports the " +
            "results to the standard output."
    ],
    showDefaultValues = true
)
class DecapsulationsCommand : Subcommand() {
    override val name: String get() = "decapsulations"

    @Option(
        names = ["--ignore-constants"],
        description = ["ignore decapsulations of constant fields"]
    )
    private var ignoreConstants: Boolean = false

    @Option(
        names = ["--min-metric-value"],
        description = [
            "ignore source files that have less decapsulations than the " +
                "specified limit."
        ]
    )
    private var minMetricValue: Int = 0

    override fun run() {
        val analyzer = HistoryAnalyzer(ignoreConstants)
        val repository = load()
        val report = analyzer.analyze(repository.getHistory())
        val files = report.files.filter { it.value >= minMetricValue }
        JsonModule.serialize(System.out, files)
    }
}
