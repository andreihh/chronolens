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

package org.metanalysis.git

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.IO
import org.metanalysis.core.versioning.Commit
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.FileNotFoundException
import java.io.IOException
import java.util.Date

class GitDriver : VersionControlSystem() {
    companion object {
        const val NAME: String = "git"
    }

    override val name: String
        get() = NAME

    @Throws(IOException::class)
    private fun execute(vararg command: String): Int = try {
        ProcessBuilder().command(*command).start().waitFor()
    } catch (e: InterruptedException) {
        throw IOException(e)
    }

    private fun String.trimQuotes(): String =
            if (length > 1 && startsWith("\"") && endsWith("\""))
                drop(1).dropLast(1)
            else this

    private fun String.split(delimiter: Char): Array<String> =
            split(delimiters = delimiter).toTypedArray()

    private val formatOpt: String
        get() = "--format=\"%H;%at;%an\""

    @Throws(IOException::class)
    private fun parseCommit(line: String): Commit = try {
        val (id, date, author) = line.split(';', limit = 3)
        Commit(id, author, Date(1000L * date.toLong()))
    } catch (e: IndexOutOfBoundsException) {
        throw IOException("Error parsing commit from '$line'!")
    }

    @Throws(IOException::class)
    private fun validateRevisionId(revisionId: String) {
        val exitCode = execute("git", "cat-file", "-e", "$revisionId^{commit}")
        require(exitCode == 0) { "Invalid revision id '$revisionId'!" }
    }

    @Throws(IOException::class)
    override fun isSupported(): Boolean = execute("git", "--version") == 0

    @Throws(IOException::class)
    override fun detectRepository(): Boolean =
            execute("git", "status", "--porcelain") == 0

    @Throws(IOException::class)
    override fun getHead(): Commit = getCommit("HEAD")

    @Throws(IOException::class)
    override fun getCommit(
            revisionId: String
    ): Commit = object : Subprocess<Commit>() {
        init {
            validateRevisionId(revisionId)
        }

        override val command: List<String>
            get() = listOf("git", "show", "--no-patch", formatOpt, revisionId)

        @Throws(IOException::class)
        override fun onSuccess(input: String): Commit {
            val line = input.split('\n').firstOrNull()
                    ?: throw IOException("Error parsing commit!")
            return parseCommit(line.trimQuotes())
        }
    }.run()

    @Throws(IOException::class)
    override fun listFiles(
            revisionId: String
    ): Set<String> = object : Subprocess<Set<String>>() {
        init {
            validateRevisionId(revisionId)
        }

        override val command: List<String>
            get() = listOf("git", "ls-tree", "--name-only", "-r", revisionId)

        override fun onSuccess(input: String): Set<String> =
                input.split('\n').filter(String::isNotBlank).toSet()
    }.run()

    @Throws(FileNotFoundException::class, IOException::class)
    override fun getFile(
            revisionId: String,
            path: String
    ): String = object : Subprocess<String>() {
        init {
            validateRevisionId(revisionId)
        }

        override val command: List<String>
            get() = listOf("git", "show", "$revisionId:$path")

        override fun onSuccess(input: String): String = input

        @Throws(FileNotFoundException::class, IOException::class)
        override fun onError(error: String): Nothing = when {
            "Path '$path' does not exist in '$revisionId'" in error ->
                throw FileNotFoundException(error)
            else -> super.onError(error)
        }
    }.run()

    @Throws(FileNotFoundException::class, IOException::class)
    override fun getFileHistory(
            revisionId: String,
            path: String
    ): List<Commit> = object : Subprocess<List<Commit>>() {
        init {
            validateRevisionId(revisionId)
        }

        override val command: List<String>
            get() = listOf(
                    *"git log --first-parent $formatOpt --reverse".split(' '),
                    revisionId,
                    "--",
                    path
            )

        @Throws(FileNotFoundException::class, IOException::class)
        override fun onSuccess(input: String): List<Commit> {
            val commits = input.split('\n')
                    .filter(String::isNotBlank)
                    .map { it.trimQuotes() }
                    .map(this@GitDriver::parseCommit)
            return if (commits.isNotEmpty()) commits
            else throw FileNotFoundException(
                    "Path '$path' doesn't exist in '$revisionId'!"
            )
        }
    }.run()
}
