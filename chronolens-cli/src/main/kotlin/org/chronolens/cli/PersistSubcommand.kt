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

package org.chronolens.cli

import java.io.IOException
import java.io.UncheckedIOException
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import org.chronolens.core.model.Revision
import org.chronolens.core.repository.Repository.HistoryProgressListener
import org.chronolens.core.repository.RepositoryConnector
import org.chronolens.core.repository.RepositoryConnector.AccessMode.RANDOM_ACCESS
import org.chronolens.core.repository.persist

@OptIn(ExperimentalCli::class)
class PersistSubcommand :
    Subcommand(
        name = "persist",
        actionDescription =
            """Connects to the repository and persists the source and history model from all the
            files that can be interpreted.

            The model is persisted in the '.chronolens' directory of the repository root."""
    ) {

    private val optionsProvider = CommandLineOptionsProvider(this)
    private val repositoryRoot by optionsProvider.repositoryRootOption()
    private val connector by lazy { RepositoryConnector.newConnector(repositoryRoot) }

    override fun execute() {
        try {
            connector.connect(RANDOM_ACCESS).persist(connector.openOrCreate(), ProgressListener())
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}

private class ProgressListener : HistoryProgressListener {
    var totalRevisionCount: Int = 0
    var revisionCount: Int = 0

    override fun onStart(revisionCount: Int) {
        this.totalRevisionCount = revisionCount
        println("Persisting $revisionCount revisions...")
    }

    override fun onRevision(revision: Revision) {
        revisionCount++
        println("Persisted revision $revisionCount / $totalRevisionCount with id '${revision.id}'!")
    }

    override fun onEnd() {
        println("Repository persisted successfully!")
    }
}
