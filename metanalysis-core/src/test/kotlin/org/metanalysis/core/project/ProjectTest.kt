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

import org.metanalysis.core.model.SourceFile
import org.metanalysis.test.assertEquals
import org.metanalysis.test.core.versioning.VersionControlSystemMock
import org.metanalysis.test.core.versioning.VersionControlSystemMock.CommitMock

import java.io.IOException
import java.util.Date

import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProjectTest {
    private fun getResource(path: String): String? =
            javaClass.getResourceAsStream(path)?.reader()?.readText()

    init {
        VersionControlSystemMock.setRepository(listOf(
                CommitMock(
                        id = "1",
                        date = Date(192345),
                        author = "name",
                        changedFiles = mapOf(
                                "resource.mock" to getResource("/resource.mock")
                        )
                ),
                CommitMock(
                        id = "2",
                        date = Date(192346),
                        author = "name",
                        changedFiles = mapOf("resource.mock" to null)
                ),
                CommitMock(
                        id = "3",
                        date = Date(192347),
                        author = "name",
                        changedFiles = mapOf("resource.mock" to "{")
                )
        ))
    }

    private val project = assertNotNull(Project.create())

    @Test fun `test get file model of non-existent file returns null`() {
        assertNull(project.getFileModel("resource.mock", "2"))
    }

    @Test fun `test get file model of empty file returns empty source file`() {
        val expected = SourceFile()
        val actual = project.getFileModel("resource.mock", "1")
        assertEquals(expected, actual)
    }

    @Test(expected = IOException::class)
    fun `test get file model with syntax errors throws`() {
        project.getFileModel("resource.mock")
    }

    @Test(expected = IOException::class)
    fun `test get file model of unrecognized extension throws`() {
        project.getFileModel("resource.java")
    }

    @Test(expected = IOException::class)
    fun `test get file model from invalid revision throws`() {
        project.getFileModel("resource.mock", "-1")
    }
}
