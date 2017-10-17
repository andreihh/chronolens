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
import org.metanalysis.core.repository.Repository
import org.metanalysis.core.serialization.JsonModule.serialize

import java.io.IOException

sealed class Command {
    companion object {
        private val commands =
                listOf(Version, Help, List, Model, History, Persist, Clean)

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

    protected fun getProject(): Repository = InteractiveRepository.connect()
            ?: throw IOException("Repository not found!")

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
        Usage: metanalysis list

        Prints all the interpretable files in the project detected in the
        current working directory.
        """.trimIndent()

        override fun execute(vararg args: String) {
            checkUsage(args.isEmpty())
            val project = getProject()
            project.listSources().forEach(::println)
        }
    }

    object Model : Command() {
        override val name: String = "model"
        override val help: String = """
        Usage: metanalysis model <path>

        Prints the interpreted code metadata from the file located at the given
        <path> in the project detected in the current working directory.
        """.trimIndent()

        override fun execute(vararg args: String) {
            checkUsage(args.size == 1)
            val project = getProject()
            val model = project.getSourceUnit(args[0])
            if (model != null) {
                serialize(System.out, model)
            } else {
                printlnErr("File couldn't be interpreted!")
            }
        }
    }

    object History : Command() {
        override val name: String = "history"
        override val help: String = """
        Usage: metanalysis history
        """.trimIndent()

        override fun execute(vararg args: String) {
            checkUsage(args.isEmpty())
            val project = getProject()
            for (transaction in project.getHistory()) {
                serialize(System.out, transaction)
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
            project.persist()
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
}
