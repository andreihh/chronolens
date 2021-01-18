/*
 * Copyright 2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.core.versioning

import org.chronolens.core.versioning.VcsProxyFactory
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.fail

/**
 * Tests for [org.chronolens.core.versioning.VcsProxy] and [VcsProxyFactory]
 * implementations.
 */
public abstract class AbstractVcsProxyTest {
    @get:Rule public val tmp: TemporaryFolder =
        TemporaryFolder.builder().assureDeletion().build()

    /**
     * Creates a repository in the given [directory] with a revision for each of
     * the [changeSets], and returns the set of VCS-internal directories created
     * in the given [directory] as a result (if any).
     *
     * The VCS-internal directories should be ignored when comparing local with
     * committed file trees.
     *
     * If no [changeSets] are specified, an empty repository (without any
     * revisions) should be created.
     */
    protected abstract fun createRepository(
        directory: File,
        changeSets: List<Map<String, String>> = emptyList(),
    ): Set<String>

    private val directory by lazy { tmp.root }
    private val vcs by lazy {
        VcsProxyFactory.detect(directory)
            ?: fail("Failed to connect to repository!")
    }

    @Test public fun detect_whenNoRepository_returnsNull() {
        assertNull(VcsProxyFactory.detect(directory))
    }

    @Test public fun getHead_whenEmptyRepository_throws() {
        createRepository(directory)

        assertFailsWith<IllegalStateException> {
            vcs.getHead()
        }
    }

    @Test public fun getRevision_whenHeadId_returnsHead() {
        createRepository(directory, TEST_CHANGE_SETS)

        val head = vcs.getHead()
        val commit = vcs.getRevision(head.id)

        assertEquals(head, commit)
    }

    @Test public fun getRevision_whenInvalidId_returnsNull() {
        createRepository(directory, TEST_CHANGE_SETS)
        val invalidRevisions = listOf("0123456789", "master-invalid", "gradle")

        for (rev in invalidRevisions) {
            assertNull(vcs.getRevision(rev))
        }
    }

    @Test public fun getChangeSet_returnsChangedFiles() {
        createRepository(directory, TEST_CHANGE_SETS)

        assertEquals(
            expected = setOf("gradle.properties"),
            actual = vcs.getChangeSet(vcs.getHead().id),
        )
    }

    @Test public fun listFiles_returnsCommittedFilesAtHead() {
        val internalDirectories = createRepository(directory, TEST_CHANGE_SETS)
        val expected = directory
            .walk()
            .onEnter { it.name !in internalDirectories }
            .filter(File::isFile)
            .map { it.path.removePrefix("${directory.absolutePath}/") }
            .toSet()

        val actual = vcs.listFiles(vcs.getHead().id)

        assertEquals(expected, actual)
    }

    @Test public fun getFile_returnsContent() {
        createRepository(directory, TEST_CHANGE_SETS)
        val path = "gradle.properties"
        val expected = File(directory, path).readText()

        val actual = vcs.getFile(vcs.getHead().id, path)

        assertEquals(expected, actual)
    }

    @Test public fun getFile_whenFileNotFound_returnsNull() {
        createRepository(directory, TEST_CHANGE_SETS)

        assertNull(vcs.getFile(vcs.getHead().id, "non-existent.txt"))
    }

    @Test public fun getFile_whenInvalidRevision_throws() {
        createRepository(directory, TEST_CHANGE_SETS)
        val path = "gradle.properties"

        assertFailsWith<IllegalArgumentException> {
            vcs.getFile("invalid-revision", path)
        }
    }

    @Test public fun getHistory_whenFileNotFound_returnsEmpty() {
        createRepository(directory, TEST_CHANGE_SETS)

        assertEquals(emptyList(), vcs.getHistory("non-existent.txt"))
    }

    @Test public fun getHistory_forFile_returnsRevisionsThatTouchedFile() {
        createRepository(directory, TEST_CHANGE_SETS)

        val history = vcs.getHistory("README.txt")

        assertEquals(1, history.size)
    }

    @Test public fun getHistory_returnsAllRevisions() {
        createRepository(directory, TEST_CHANGE_SETS)

        val history = vcs.getHistory()

        assertEquals(2, history.size)
        assertEquals(vcs.getHead(), history.last())
    }
}

private val TEST_CHANGE_SETS =
    listOf(
        mapOf(
            "gradle.properties" to "org.gradle.daemon=false",
            "README.txt" to "This is a test repository.",
        ),
        mapOf("gradle.properties" to "org.gradle.daemon=true"),
    )
