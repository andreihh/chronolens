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

import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import org.chronolens.core.repository.RepositoryConnector
import java.io.IOException
import java.io.UncheckedIOException

@OptIn(ExperimentalCli::class)
class CleanSubcommand :
    Subcommand(
        name = "clean",
        actionDescription = "Deletes any persisted repositories from the given directory."
    ) {

    private val optionsProvider = CommandLineOptionsProvider(this)
    private val repositoryRoot by optionsProvider.repositoryRootOption()

    override fun execute() {
        try {
            RepositoryConnector.newConnector(repositoryRoot).delete()
            println("Cleared persisted repositories!")
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}
