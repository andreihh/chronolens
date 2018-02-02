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

import org.metanalysis.core.repository.InteractiveRepository
import org.metanalysis.core.repository.PersistentRepository
import org.metanalysis.core.repository.PersistentRepository.Companion.persist
import org.metanalysis.core.repository.PersistentRepository.ProgressListener
import org.metanalysis.core.repository.Repository.Companion.isValidPath
import org.metanalysis.core.repository.Repository.Companion.isValidTransactionId
import java.io.IOException

sealed class Command {
    companion object {
        @JvmStatic
        protected val commands =
            listOf(Version, Help, List, RevList, Model, Persist, Clean)

        @JvmStatic
        operator fun invoke(name: String): Command? =
            commands.firstOrNull { it.name == name }
    }

    abstract val name: String

    abstract val help: String

    abstract fun execute(vararg args: String)

    protected fun checkUsage(value: Boolean) {
        if (!value) {
            usage(help)
        }
    }

    protected fun getProject(): InteractiveRepository =
        InteractiveRepository.connect()
            ?: throw IOException("Repository not found!")
}

object Version : Command() {
    override val name: String = "version"
    override val help: String = """
        Usage: metanalysis version

        Prints the installed version of metanalysis.
        """.trimIndent()

    override fun execute(vararg args: String) {
        println("metanalysis 0.2")
    }
}

object Help : Command() {
    override val name: String = "help"
    override val help: String = """
        Usage: metanalysis help [<command>]

        Show usage instructions for the given <command>, or list the available
        commands if no arguments are provided.
        """.trimIndent()

    override fun execute(vararg args: String) {
        checkUsage(args.size <= 1)
        if (args.isEmpty()) {
            println("Usage: metanalysis <command> <args>")
            println("\nAvailable commands:")
            commands.map(Command::name).forEach { println("- $it") }
        } else {
            val command = Command(args[0])
            if (command != null) {
                println(command.help)
            } else {
                printlnErr("Unknown command!")
            }
        }
    }
}

object List : Command() {
    override val name: String = "list"
    override val help: String = """
        Usage: metanalysis list [<revision>]

        Prints all the interpretable files in the project detected in the
        current working directory from the specified <revision> (default value
        is the <head> revision).
        """.trimIndent()

    override fun execute(vararg args: String) {
        checkUsage(args.size in 0..1)
        val project = getProject()
        val revision = if (args.size == 1) args[0] else project.getHeadId()
        checkUsage(isValidTransactionId(revision))
        project.listSources(revision).forEach(::println)
    }
}

object RevList : Command() {
    override val name: String = "rev-list"
    override val help: String = """
        Usage: metanalysis rev-list

        Prints all the revisions in the project detected in the current working
        directory.
        """.trimIndent()

    override fun execute(vararg args: String) {
        checkUsage(args.isEmpty())
        val project = getProject()
        project.listRevisions().forEach(::println)
    }
}

object Model : Command() {
    override val name: String = "model"
    override val help: String = """
        Usage: metanalysis model <path> [<revision>]

        Prints the interpreted code metadata from the file located at the given
        <path> as it is found in the given <revision> (the default value is the
        <head> revision) of the project detected in the current working
        directory.
        """.trimIndent()

    override fun execute(vararg args: String) {
        checkUsage(args.size in 1..2)
        val project = getProject()
        val path = args[0]
        val revision = if (args.size == 2) args[1] else project.getHeadId()
        checkUsage(isValidPath(path))
        checkUsage(isValidTransactionId(revision))
        val model = project.getSource(path, revision)
        if (model != null) {
            PrettyPrinterVisitor(System.out).visit(model)
        } else {
            printlnErr("File couldn't be interpreted or doesn't exist!")
        }
    }
}

object Persist : Command() {
    override val name: String = "persist"
    override val help: String = """
        Usage: metanalysis persist

        Connects to the repository detected in the current working directory and
        persists the code metadata and history from all the files which can be
        interpreted.

        The project is persisted in the '.metanalysis' directory from the
        current working directory.
        """.trimIndent()

    override fun execute(vararg args: String) {
        checkUsage(args.isEmpty())
        val project = getProject()
        project.persist(object : ProgressListener {
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

object Clean : Command() {
    override val name: String = "clean"
    override val help: String = """
        Usage: metanalysis clean

        Deletes the previously persisted project from the current working
        directory, if it exists.
        """.trimIndent()

    override fun execute(vararg args: String) {
        checkUsage(args.isEmpty())
        PersistentRepository.clean()
    }
}
