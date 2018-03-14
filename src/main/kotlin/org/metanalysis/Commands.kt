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

package org.metanalysis

import org.metanalysis.MainCommand.Subcommand
import org.metanalysis.core.repository.InteractiveRepository
import org.metanalysis.core.repository.PersistentRepository
import org.metanalysis.core.repository.PersistentRepository.Companion.persist
import org.metanalysis.core.repository.PersistentRepository.ProgressListener
import org.metanalysis.core.repository.Repository.Companion.isValidPath
import org.metanalysis.core.repository.Repository.Companion.isValidRevisionId
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.ParentCommand

abstract class AbstractCommand : Runnable {
    @Option(
        names = ["-h", "--help"],
        usageHelp = true,
        description = ["display this help message"]
    )
    private var usageRequested: Boolean = false
}

@Command(
    name = "metanalysis",
    version = ["0.2"],
    description = [
        "Metanalysis is a software evolution analysis tool that inspects the "
            + "repository detected in the current working directory."
    ],
    subcommands = [
        List::class, RevList::class, Model::class, Persist::class, Clean::class
    ]
)
class MainCommand : AbstractCommand() {
    @Option(
        names = ["-V", "--version"],
        versionHelp = true,
        description = ["display version information and exit"]
    )
    private var versionRequested: Boolean = false

    internal lateinit var repository: InteractiveRepository
        private set

    override fun run() {
        repository = InteractiveRepository.connect()
            ?: exit("Repository not found!")
    }

    abstract class Subcommand : AbstractCommand() {
        @ParentCommand
        private lateinit var parent: MainCommand

        protected val repository: InteractiveRepository
            get() = parent.repository
    }
}

@Command(
    name = "list",
    description = [
        "Prints all the interpretable files of the repository from the "
            + "specified <revision>."
    ]
)
class List : Subcommand() {
    @Option(
        names = ["-r", "--revision"],
        description = ["the inspected revision (default: the <head> revision)"]
    )
    private var revisionId: String? = null
    private val revision: String get() = revisionId ?: repository.getHeadId()

    override fun run() {
        if (!isValidRevisionId(revision)) exit("Invalid revision '$revision'!")
        repository.listSources(revision).forEach(::println)
    }
}

@Command(
    name = "rev-list",
    description = ["Prints all revisions of the repository."]
)
class RevList : Subcommand() {
    override fun run() {
        repository.listRevisions().forEach(::println)
    }
}

@Command(
    name = "model",
    description = [
        "Prints the interpreted code metadata from the file located at the "
            + "given <path> as it is found in the given <revision> of the "
            + "repository."
    ]
)
class Model : Subcommand() {
    @Option(
        names = ["-f", "--file"],
        description = ["the inspected file"],
        required = true
    )
    private lateinit var path: String

    @Option(
        names = ["-r", "--revision"],
        description = ["the inspected revision (default: the <head> revision)"]
    )
    private var revisionId: String? = null
    private val revision: String get() = revisionId ?: repository.getHeadId()

    override fun run() {
        if (!isValidPath(path)) exit("Invalid file path '$path'!")
        if (!isValidRevisionId(revision)) exit("Invalid revision '$revision'!")
        val model = repository.getSource(path, revision)
        if (model != null) {
            PrettyPrinterVisitor(System.out).visit(model)
        } else {
            exit("File couldn't be interpreted or doesn't exist!")
        }
    }
}

@Command(
    name = "persist",
    description = [
        "Connects to the repository and persists the code and history metadata "
            + "from all the files that can be interpreted.",
        "The repository is persisted in the '.metanalysis' directory from the "
            + "current working directory."
    ]
)
class Persist : Subcommand() {
    override fun run() {
        repository.persist(object : ProgressListener {
            private var sources = 0
            private var transactions = 0
            private var i = 0

            override fun onSnapshotStart(headId: String, sourceCount: Int) {
                println("Persisting snapshot '$headId'...")
                sources = sourceCount
                i = 0
            }

            override fun onSourcePersisted(path: String) {
                i++
                print("Persisted $i / $sources sources...\r")
            }

            override fun onSnapshotEnd() {
                println()
                println("Done!")
            }

            override fun onHistoryStart(revisionCount: Int) {
                println("Persisting transactions...")
                transactions = revisionCount
                i = 0
            }

            override fun onTransactionPersisted(id: String) {
                i++
                print("Persisted $i / $transactions transactions...\r")
            }

            override fun onHistoryEnd() {
                println()
                println("Done!")
            }
        })
    }
}

@Command(
    name = "clean",
    description = [
        "Deletes the previously persisted repository from the current working "
            + "directory, if it exists."
    ]
)
class Clean : Subcommand() {
    override fun run() {
        PersistentRepository.clean()
    }
}
