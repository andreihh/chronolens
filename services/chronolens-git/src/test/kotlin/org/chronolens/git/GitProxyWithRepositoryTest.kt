/*
 * Copyright 2018-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.git

import org.chronolens.core.versioning.VcsProxyFactory
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class GitProxyWithRepositoryTest {
    companion object {
        @ClassRule
        @JvmField
        val tmp = TemporaryFolder.builder().assureDeletion().build()

        @BeforeClass
        @JvmStatic
        fun cloneRepository() {
            // clone(tmp.root, "https://github.com/andreihh/chronolens.git")
            init(tmp.root)
            commit(
                directory = tmp.root,
                changeSet = mapOf(
                    "gradle.properties" to "org.gradle.daemon=false",
                    "README.txt" to "This is a test repository.",
                )
            )
            commit(
                directory = tmp.root,
                changeSet = mapOf(
                    "gradle.properties" to "org.gradle.daemon=true,"
                )
            )
        }
    }

    private val git = GitProxyFactory().connect(tmp.root)
        ?: fail("Couldn't connect to git repository!")

    @Test fun `test detect`() {
        assertTrue(VcsProxyFactory.detect(tmp.root) is GitProxy)
    }

    @Test fun getRevision_returnsSameIdForEquivalentLabels() {
        val head = git.getHead()
        val commit = git.getRevision("HEAD")
        val branch = git.getRevision("master")

        assertEquals(head, commit)
        assertEquals(head, branch)
    }

    @Test fun getRevision_whenInvalidId_returnsNull() {
        val invalidRevisions = listOf("0123456", "master-invalid", "gradle")

        for (rev in invalidRevisions) {
            assertNull(git.getRevision(rev))
        }
    }

    /*@Test fun `test get revision`() {
        val head = git.getHead()
        val commit = git.getRevision("HEAD")
        val branch = git.getRevision("master")
        assertEquals(head, commit)
        assertEquals(head, branch)
        val invalidRevisions = listOf("0123456", "master-invalid", "gradle")
        for (rev in invalidRevisions) {
            assertNull(git.getRevision(rev))
        }
    }*/

    @Test fun getChangeSet_returnsChangedFiles() {
        assertEquals(
            setOf("gradle.properties"),
            git.getChangeSet(git.getHead().id)
        )
    }

    /*@Test fun `test get change set`() {
        val headId = git.getHead().id
        assertNotEquals(emptySet(), git.getChangeSet(headId))
    }*/

    @Test fun `test list files`() {
        val expected = tmp.root
            .walk().onEnter { it.name != ".git" }
            .filter(File::isFile)
            .map { it.path.removePrefix("${tmp.root.absolutePath}/") }
            .toSet()

        val actual = git.listFiles(git.getHead().id)

        assertEquals(expected, actual)
    }

    @Test fun getFile_returnsContent() {
        val path = "gradle.properties"
        val expected = File(tmp.root, path).readText()

        val actual = git.getFile(git.getHead().id, path)

        assertEquals(expected, actual)
    }

    @Test fun getFile_whenFileNotFound_returnsNull() {
        assertNull(git.getFile(git.getHead().id, "non-existent.txt"))
    }

    @Test fun getFile_whenInvalidRevision_throws() {
        val path = "gradle.properties"

        assertFailsWith<IllegalArgumentException> {
            git.getFile("invalid-revision", path)
        }
    }

    @Test fun getHistory_whenFileNotFound_returnsEmpty() {
        assertEquals(emptyList(), git.getHistory("non-existent.txt"))
    }

    @Test fun getHistory_forFile_returnsCommitsThatTouchedFile() {
        val history = git.getHistory("README.txt")

        assertEquals(1, history.size)
    }

    @Test fun getHistory_returnsAllCommits() {
        val history = git.getHistory()

        assertEquals(2, history.size)
        assertEquals(git.getHead(), history.last())
    }

    /*@Test fun `test get file`() {
        val path = "services/chronolens-git/build.gradle.kts"
        val expected = File(tmp.root, path).readText()
        val actual = git.getFile(revisionId = "HEAD", path = path)
        assertEquals(expected, actual)
        assertNull(git.getFile(revisionId = "HEAD", path = "non-existent.txt"))
        assertFailsWith<IllegalArgumentException> {
            git.getFile(revisionId = "invalid-revision", path = path)
        }
    }

    @Test fun `test get history`() {
        assertEquals(emptyList(), git.getHistory("non-existent.txt"))
        assertNotEquals(emptyList(), git.getHistory("README.md"))
        assertNotEquals(emptyList(), git.getHistory())
    }*/
}
