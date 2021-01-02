/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class GitProxyWithRepositoryTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun cloneRepository() {
            clone("https://github.com/andreihh/chronolens.git")
        }

        @AfterClass
        @JvmStatic
        fun cleanRepository() {
            clean()
        }
    }

    private val git = GitProxyFactory().connect()
        ?: fail("Couldn't connect to git repository!")

    @Test fun `test detect`() {
        assertTrue(VcsProxyFactory.detect() is GitProxy)
    }

    @Test fun `test get revision`() {
        val head = git.getHead()
        val commit = git.getRevision("HEAD")
        val branch = git.getRevision("master")
        assertEquals(head, commit)
        assertEquals(head, branch)
        val invalidRevisions = listOf("0123456", "master-invalid", "gradle")
        for (rev in invalidRevisions) {
            assertNull(git.getRevision(rev))
        }
    }

    @Test fun `test get change set`() {
        val headId = git.getHead().id
        assertNotEquals(emptySet(), git.getChangeSet(headId))
    }

    @Test fun `test list files`() {
        val expected = File("./")
            .walk().onEnter { it.name != ".git" }
            .filter(File::isFile)
            .map { it.path.removePrefix("./") }
            .toSet()
        val headId = git.getHead().id
        val actual = git.listFiles(headId)
        assertEquals(expected, actual)
    }

    @Test fun `test get file`() {
        val path = "services/chronolens-git/build.gradle.kts"
        val expected = File(path).readText()
        val actual = git.getFile(revisionId = "HEAD", path = path)
        assertEquals(expected, actual)
        assertNull(git.getFile(revisionId = "HEAD", path = "non-existent.txt"))
        assertFailsWith<IllegalArgumentException> {
            git.getFile(revisionId = "invalid-revision", path = path)
        }
    }

    @Ignore // TODO: figure out why it fails on Travis.
    @Test fun `test get history`() {
        assertEquals(emptyList(), git.getHistory("non-existent.txt"))
        assertNotEquals(emptyList(), git.getHistory("README.md"))
        assertNotEquals(emptyList(), git.getHistory())
    }
}
