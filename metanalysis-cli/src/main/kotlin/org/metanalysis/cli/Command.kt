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

package org.metanalysis.cli

import org.metanalysis.core.project.InteractiveProject
import org.metanalysis.core.project.PersistentProject
import org.metanalysis.core.project.PersistentProject.Companion.persist
import org.metanalysis.core.project.Project
import org.metanalysis.core.serialization.JsonDriver.serialize

import java.io.IOException

sealed class Command {
    companion object {
        private val commands = listOf(
                Version,
                Help,
                List,
                Model,
                History,
                Persist,
                Clean
        )

        @JvmStatic operator fun invoke(name: String): Command =
                commands.firstOrNull { it.name == name }
                        ?: throw NoSuchElementException("")
    }

    abstract val name: String

    abstract val help: String

    abstract fun execute(vararg args: String): Unit

    protected fun getProject(): Project = InteractiveProject.connect()
            ?: throw IOException("Project not found!")

    object Version : Command() {
        override val name: String = "version"
        override val help: String = """
        Prints the installed version of metanalysis.
        """

        override fun execute(vararg args: String) {
            println("metanalysis 0.1.7")
        }
    }

    object Help : Command() {
        override val name: String = "help"
        override val help: String = """
        Parameters: [<command>]

        Show usage instructions for the given <command>, or list the available
        commands if no arguments are provided.
        """

        override fun execute(vararg args: String) {
            require(args.size <= 1)
            if (args.isEmpty()) {
                println("Usage: metanalysis <command> <args>")
                commands.map(Command::name).forEach(::println)
            } else {
                println(Command(args[0]).help)
            }
        }
    }

    object List : Command() {
        override val name: String = "list"
        override val help: String = """
        Prints all the files in the project detected in the current working
        directory.
        """

        override fun execute(vararg args: String) {
            val project = getProject()
            project.listFiles().forEach(::println)
        }
    }

    object Model : Command() {
        override val name: String = "model"
        override val help: String = """
        Parameters: <path>

        Prints the interpreted code metadata from the file located at the given
        <path> in the project detected in the current working directory.
        """

        override fun execute(vararg args: String) {
            require(args.size == 1)
            val project = getProject()
            serialize(System.out, project.getFileModel(args[0]))
        }
    }

    object History : Command() {
        override val name: String = "history"
        override val help: String = """
        """

        override fun execute(vararg args: String) {
            if (args.size != 1) {
                throw IllegalUsageException("")
            }
            val project = getProject()
            serialize(System.out, project.getFileHistory(args[0]))
        }
    }

    object Persist : Command() {
        override val name: String = "persist"
        override val help: String = """
        Connects to the repository detected in the current working directory and
        persists the code metadata and history from all the files which can be
        interpreted.

        The project is persisted in the '.metanalysis' directory from the
        current working directory.
        """

        override fun execute(vararg args: String) {
            val project = getProject()
            project.persist()
        }
    }

    object Clean : Command() {
        override val name: String = "clean"
        override val help: String = """
        Deletes the previously persisted project from the current working
        directory, if it exists.
        """

        override fun execute(vararg args: String) {
            PersistentProject.clean()
        }
    }
}
