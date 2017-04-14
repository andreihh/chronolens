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

package org.metanalysis.core.project

import org.junit.Test

import org.metanalysis.core.delta.Transaction.Companion.apply
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.project.Project.HistoryEntry
import org.metanalysis.test.assertEquals
import org.metanalysis.test.core.versioning.VersionControlSystemMock
import org.metanalysis.test.core.versioning.VersionControlSystemMock.CommitMock

import java.io.IOException
import java.util.Date

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProjectTest {
    private fun getResource(path: String): String? =
            javaClass.getResourceAsStream(path)?.reader()?.readText()

    private val path = "resource.mock"
    private val resourceMock = getResource("/resource.mock")
    private val genericTypeResolver1 =
            getResource("/GenericTypeResolver-v1.mock")
    private val genericTypeResolver2 =
            getResource("/GenericTypeResolver-v2.mock")

    init {
        VersionControlSystemMock.setRepository(listOf(
                CommitMock(
                        id = "1",
                        date = Date(192345),
                        author = "name",
                        changedFiles = mapOf(path to resourceMock)
                ),
                CommitMock(
                        id = "2",
                        date = Date(192346),
                        author = "name",
                        changedFiles = mapOf(path to null)
                ),
                CommitMock(
                        id = "3",
                        date = Date(192347),
                        author = "name",
                        changedFiles = mapOf(path to "{")
                ),
                CommitMock(
                        id = "4",
                        date = Date(192348),
                        author = "name",
                        changedFiles = mapOf(path to genericTypeResolver1)
                ),
                CommitMock(
                        id = "5",
                        date = Date(192349),
                        author = "name",
                        changedFiles = mapOf(path to genericTypeResolver2)
                )
        ))
    }

    private val project = assertNotNull(Project.create())

    @Test fun `test get file model of non-existent file returns null`() {
        assertNull(project.getFileModel(path, "2"))
    }

    @Test fun `test get file model of empty file returns empty source file`() {
        val expected = SourceFile()
        val actual = project.getFileModel(path, "1")
        assertEquals(expected, actual)
    }

    @Test(expected = IOException::class)
    fun `test get file model with syntax errors throws`() {
        project.getFileModel(path, "3")
    }

    @Test(expected = IOException::class)
    fun `test get file model of unrecognized extension throws`() {
        project.getFileModel("resource.java")
    }

    @Test(expected = IOException::class)
    fun `test get file model from invalid revision throws`() {
        project.getFileModel(path, "-1")
    }

    @Test fun `test apply file history transactions returns correct model`() {
        val history = project.getFileHistory(path)
        val transactions = history.mapNotNull(HistoryEntry::transaction)
        val expected = project.getFileModel(path)
        val actual = SourceFile().apply(transactions)
        assertEquals(expected, actual)
    }

    @Test fun `test list files returns correct set of files`() {
        val expected = setOf(path)
        val actual = project.listFiles()
        assertEquals(expected, actual)
    }
}
