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

package org.chronolens.interactive

import org.chronolens.core.analysis.Analyzer
import org.chronolens.core.analysis.AnalyzerSpec
import org.chronolens.core.analysis.OptionsProvider
import org.chronolens.core.analysis.Report
import org.chronolens.core.model.Revision
import org.chronolens.core.model.RevisionId
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.Repository.HistoryProgressListener
import org.chronolens.core.repository.RepositoryConnector
import org.chronolens.core.repository.RepositoryConnector.AccessMode
import org.chronolens.core.repository.RepositoryConnector.AccessMode.RANDOM_ACCESS
import org.chronolens.core.repository.persist
import java.io.File

public class PersistSpec : AnalyzerSpec {
    override val name: String
        get() = "persist"

    override val description: String
        get() = """
        Connects to the repository and persists the source and history model from all the files that
        can be interpreted.

        The model is persisted in the '.chronolens' directory from the current working directory.
        """.trimIndent()

    override fun create(optionsProvider: OptionsProvider): PersistAnalyzer =
        PersistAnalyzer(optionsProvider)
}

public class PersistAnalyzer(optionsProvider: OptionsProvider) : Analyzer(optionsProvider) {
    override val accessMode: AccessMode
        get() = RANDOM_ACCESS

    override fun analyze(repository: Repository): PersistReport {
        val listener = ProgressListener()
        repository.persist(RepositoryConnector.newConnector(File(".")).openOrCreate(), listener)
        return PersistReport(listener.headId, listener.revisionCount)
    }

    private class ProgressListener : HistoryProgressListener {
        lateinit var headId: RevisionId
        var revisionCount: Int = 0

        override fun onStart(revisionCount: Int) {
            this.revisionCount = revisionCount
        }

        override fun onRevision(revision: Revision) {
            headId = revision.id
        }

        override fun onEnd() {
            System.err.println("")
        }
    }
}

public data class PersistReport(val headId: RevisionId, val revisionCount: Int) : Report
