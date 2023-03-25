/*
 * Copyright 2022-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.repository

import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import org.chronolens.api.analysis.Report
import org.chronolens.api.database.RepositoryDatabase
import org.chronolens.api.serialization.SerializationException
import org.chronolens.api.serialization.deserialize
import org.chronolens.core.serialization.JsonModule
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId

internal class RepositoryFileStorage(rootDirectory: File) : RepositoryDatabase {
  private val storageDirectory = File(rootDirectory, STORAGE_ROOT_DIRECTORY)
  private val historyFile: File = File(storageDirectory, "history.json")
  private val revisionsDirectory: File = File(storageDirectory, "revisions")

  private fun getRevisionFile(revisionId: RevisionId): File =
    File(revisionsDirectory, "$revisionId.json")

  private fun readRevision(revisionId: RevisionId): Revision =
    try {
      JsonModule.deserialize(getRevisionFile(revisionId))
    } catch (e: IOException) {
      throw UncheckedIOException(e)
    }

  @Throws(IOException::class)
  private fun writeRevision(revision: Revision) {
    JsonModule.serialize(getRevisionFile(revision.id), revision)
  }

  @Throws(IOException::class)
  override fun readHistoryIds(): List<RevisionId> = JsonModule.deserialize(historyFile)

  @Throws(IOException::class)
  override fun readHistory(): Sequence<Revision> = readHistoryIds().asSequence().map(::readRevision)

  @Throws(IOException::class)
  override fun writeHistory(revisions: Sequence<Revision>) {
    mkdirs(revisionsDirectory)
    val revisionIds = arrayListOf<RevisionId>()
    revisions.forEach { revision ->
      revisionIds += revision.id
      writeRevision(revision)
    }
    JsonModule.serialize(historyFile, revisionIds)
  }

  override fun appendHistory(revisions: Sequence<Revision>) {
    TODO("Not yet implemented")
  }

  override fun readReport(name: String): Report {
    TODO("Not yet implemented")
  }

  override fun writeReport(report: Report) {
    TODO("Not yet implemented")
  }

  override fun close() {}

  companion object {
    /** The directory within the repository root where all storage files should be stored. */
    const val STORAGE_ROOT_DIRECTORY = ".chronolens"
  }
}

/**
 * Checks that the given [file] exists and is a file.
 *
 * @throws IllegalStateException if the given [file] doesn't exist or is not a file
 */
private fun checkFileExists(file: File) {
  check(file.isFile) { "File '$file' does not exist or is not a file!" }
}

/**
 * Creates the given [directory] and any parent directories, if necessary.
 *
 * @throws IOException if the given [directory] couldn't be created
 */
@Throws(IOException::class)
private fun mkdirs(directory: File) {
  if (!directory.exists() && !directory.mkdirs()) {
    throw IOException("Failed to create directory '$directory'!")
  }
}

/**
 * Delegates to [JsonModule.serialize].
 *
 * @throws IllegalStateException if the serialization failed with a [SerializationException] or the
 * given [dst] file doesn't exist or is not a file
 * @throws IOException if any I/O errors occur
 */
@Throws(IOException::class)
private fun JsonModule.serialize(dst: File, value: Any) {
  try {
    checkFileExists(dst)
    dst.outputStream().use { serialize(it, value) }
  } catch (e: SerializationException) {
    throw IllegalStateException(e)
  }
}

/**
 * Delegates to [JsonModule.deserialize].
 *
 * @throws IllegalStateException if the deserialization failed with a [SerializationException] or
 * the given [src] file doesn't exist or is not a file
 * @throws IOException if any I/O errors occur
 */
@Throws(IOException::class)
private inline fun <reified T : Any> JsonModule.deserialize(src: File): T =
  try {
    checkFileExists(src)
    src.inputStream().use { deserialize(it) }
  } catch (e: SerializationException) {
    throw IllegalStateException(e)
  }
