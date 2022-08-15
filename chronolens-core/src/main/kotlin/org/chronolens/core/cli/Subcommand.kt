/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.cli

import java.io.File
import org.chronolens.core.model.QualifiedSourceNodeId
import org.chronolens.core.model.RevisionId
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.RepositoryConnector
import org.chronolens.core.repository.RepositoryConnector.AccessMode.FAST_HISTORY
import org.chronolens.core.repository.RepositoryConnector.AccessMode.RANDOM_ACCESS

/**
 * An abstract subcommand of a main command-line interface executable. Implementations must have the
 * [Command] annotation.
 *
 * Subcommands must have a public no-arg constructor and must supply an entry in the
 * `META-INF/services/org.chronolens.core.cli.Subcommand` configuration file.
 */
public abstract class Subcommand : Command() {
    override val name: String
        get() = getCommandName()
    final override val version: String?
        get() = null
    final override val standardHelpOptions: Boolean
        get() = false

    protected val repositoryDirectory: String by
        option<String>("--repo-dir").help("the repository directory").defaultValue(".")

    private fun getCommandName(): String {
        val className =
            this::class.simpleName ?: error("Command '${this::class}' must have a name!")
        return className.removeSuffix("Command").words().joinToString(separator = "-")
    }

    /** Returns the interactive repository from the current working directory. */
    protected fun connect(): Repository =
        RepositoryConnector.newConnector(File(repositoryDirectory)).connect(RANDOM_ACCESS)

    /** Returns the persistent repository from the current working directory. */
    protected fun load(): Repository =
        RepositoryConnector.newConnector(File(repositoryDirectory)).connect(FAST_HISTORY)

    protected fun RequiredOption<String>.validateId(): RequiredOption<String> = validate { id ->
        require(QualifiedSourceNodeId.isValid(id)) { "Invalid id '$id'!" }
    }

    protected fun NullableOption<String>.validateRevision(
        repository: () -> Repository
    ): NullableOption<String> = validate { revision ->
        require(RevisionId.isValid(revision)) { "Invalid revision '$revision'!" }
        val revisionExists = RevisionId(revision) in repository().listRevisions()
        require(revisionExists) { "Revision '$revision' doesn't exist!" }
    }
}
