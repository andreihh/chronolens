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

import java.io.IOException
import java.util.Date
import kotlin.test.assertFailsWith

import kotlin.test.assertEquals
import kotlin.test.assertNull

//@Ignore
class InteractiveProjectTest /*: ProjectTest()*/ {
    /*override val expectedProject: ProjectMock
        get() = TODO("not implemented")

    override val actualProject: Project
        get() = TODO("not implemented")*/

    private fun getResource(path: String): String? =
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
        InteractiveProject(VersionControlSystemMock())
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

    @Test fun `test connect with uninitialized repository returns null`() {
        VersionControlSystemMock.resetRepository()
        assertNull(Project.connect())
    }

    /*@Test(expected = IllegalStateException::class)
    fun `test connect with empty repository throws`() {
        VersionControlSystemMock.setRepository(emptyList())
        assertFailsWith<IllegalStateException> { Project.connect() }
    }*/

    /*@Test fun `test get file model of non-existent file returns null`() {
        assertNull(project.getFileModel(path))
    }*/

    /*@Test fun `test get file model of empty file returns empty source file`() {
        val expected = SourceFile()
        val actual = project.getFileModel(path)
        assertEquals(expected, actual)
    }*/

    /*@Test(expected = IOException::class)
    fun `test get file model with syntax errors throws`() {
        project.getFileModel(path)
    }*/

    @Test(expected = IOException::class)
    fun `test get file model of unrecognized extension throws`() {
        project.getFileModel("resource.java")
    }

    @Test fun `test apply file history transactions returns correct model`() {
        val history = project.getFileHistory(path)
        val transactions = history.mapNotNull(HistoryEntry::transaction)
        val expected = project.getFileModel(path)
        val actual = SourceFile().apply(transactions)
        assertEquals(expected, actual)
    }

    @Test fun `test list files returns correct set of files`() {
        val expected = setOf(path, "resource.txt")
        val actual = project.listFiles()
        assertEquals(expected, actual)
    }
}
