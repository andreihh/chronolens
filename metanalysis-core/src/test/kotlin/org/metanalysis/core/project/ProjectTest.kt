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

import org.junit.Ignore
import org.junit.Test

import org.metanalysis.core.delta.Transaction.Companion.apply
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.project.Project.HistoryEntry
import org.metanalysis.test.assertEquals
import org.metanalysis.test.core.project.ProjectMock
import org.metanalysis.test.core.versioning.VersionControlSystemMock
import org.metanalysis.test.core.versioning.VersionControlSystemMock.CommitMock

import java.io.FileNotFoundException
import java.io.IOException
import java.util.Date

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Ignore
abstract class ProjectTest {
    protected abstract val expectedProject: ProjectMock
    protected abstract val actualProject: Project

    @Test fun `test equal projects`() {
        assertEquals(expectedProject, actualProject)
    }

    @Test fun `test get non-existent file model throws`() {
        assertFailsWith<FileNotFoundException> {
            actualProject.getFileModel("non-existent.txt")
        }
    }

    @Test fun `test get non-existent file history throws`() {
        assertFailsWith<FileNotFoundException> {
            actualProject.getFileHistory("non-existent.txt")
        }
    }

    /*private fun getResource(path: String): String? =
            javaClass.getResourceAsStream(path)?.reader()?.readText()

    private val author = "name"
    private val path = "resource.mock"
    private val resourceMock = getResource("/resource.mock")
    private val genericTypeResolver1 =
            getResource("/GenericTypeResolver-v1.mock")
    private val genericTypeResolver2 =
            getResource("/GenericTypeResolver-v2.mock")
    private val project by lazy {
        initializeRepository()
        Project(VersionControlSystemMock())
    }

    private fun initializeRepository() {
        VersionControlSystemMock.setRepository(listOf(
                CommitMock(
                        id = "1",
                        date = Date(1),
                        author = author,
                        changedFiles = mapOf(path to resourceMock)
                ),
                CommitMock(
                        id = "2",
                        date = Date(2),
                        author = author,
                        changedFiles = mapOf(path to null)
                ),
                CommitMock(
                        id = "3",
                        date = Date(3),
                        author = author,
                        changedFiles = mapOf(path to "{")
                ),
                CommitMock(
                        id = "4",
                        date = Date(4),
                        author = author,
                        changedFiles = mapOf(path to genericTypeResolver1)
                ),
                CommitMock(
                        id = "5",
                        date = Date(5),
                        author = author,
                        changedFiles = mapOf(
                                path to genericTypeResolver2,
                                "resource.txt" to "{}"
                        )
                )
        ))
    }

    @Test fun `test create with uninitialized repository returns null`() {
        VersionControlSystemMock.resetRepository()
        assertNull(Project.connect())
    }

    @Test(expected = IllegalStateException::class)
    fun `test create with empty repository throws`() {
        VersionControlSystemMock.setRepository(emptyList())
        Project.connect()
    }

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
    }*/

    /*@Test fun `test diff between absent file in both revisions returns null`() {
        val transaction = project.getFileDiff(path, "2", "2")
        assertNull(transaction)
    }

    @Test fun `test apply file diff transaction returns correct model`() {
        val transaction = project.getFileDiff(path, "4", "5")
        val expected = project.getFileModel(path, "5")
        val actual = project.getFileModel(path, "4")?.apply(transaction)
        assertEquals(expected, actual)
    }*/

    /*@Test fun `test apply file history transactions returns correct model`() {
        val history = project.getFileHistory(path, "5")
        val transactions = history.mapNotNull(HistoryEntry::transaction)
        val expected = project.getFileModel(path, "5")
        val actual = SourceFile().apply(transactions)
        assertEquals(expected, actual)
    }

    @Test fun `test list files returns correct set of files`() {
        val expected = setOf(path, "resource.txt")
        val actual = project.listFiles("5")
        assertEquals(expected, actual)
    }*/
}
