/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.versioning.VcsProxy
import org.chronolens.core.versioning.VcsProxyFactory
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Tests for [VcsProxy] and [VcsProxyFactory] implementations. */
public abstract class AbstractVcsProxyTest {
    @get:Rule public val tmp: TemporaryFolder =
        TemporaryFolder.builder().assureDeletion().build()

    /**
     * Creates a repository in the given [directory] with the change sets of the given [revisions],
     * and returns a [VcsProxy] connection to the repository.
     *
     * If no [revisions] are specified, an empty repository (without any revisions) should be
     * created (even though that is an invalid repository state).
     */
    protected abstract fun createRepository(
        directory: File,
        vararg revisions: VcsChangeSet
    ): VcsProxy

    protected fun createRepository(vararg revisions: VcsChangeSet): VcsProxy =
        createRepository(tmp.root, *revisions)

    @Test
    public fun getHead_whenEmptyRepository_throws() {
        val vcs = createRepository()

        assertFailsWith<IllegalStateException> {
            vcs.getHead()
        }
    }

    @Test
    public fun getRevision_whenHeadId_returnsHead() {
        val vcs = createRepository(
            vcsRevision {
                change("README.txt", "This is a test repository.")
            },
            vcsRevision {
                delete("README.txt")
            },
        )

        assertEquals(expected = vcs.getHead(), actual = vcs.getRevision(vcs.getHead().id))
    }

    @Test
    public fun getRevision_whenInvalidId_returnsNull() {
        val vcs = createRepository(
            vcsRevision {
                change("README.txt", "This is a test repository.")
            },
            vcsRevision {
                delete("README.txt")
            },
        )
        val invalidRevisionIds = listOf("0123456789", "master-invalid", "gradle")

        for (revisionId in invalidRevisionIds) {
            assertNull(vcs.getRevision(revisionId))
        }
    }

    @Test
    public fun getChangeSet_returnsChangedFiles() {
        val vcs = createRepository(
            vcsRevision {
                change("README.txt", "This is a test repository.")
            },
            vcsRevision {
                change("gradle.properties", "org.gradle.daemon=true")
            }
        )

        assertEquals(
            expected = setOf("gradle.properties"),
            actual = vcs.getChangeSet(vcs.getHead().id)
        )
    }

    @Test
    public fun listFiles_returnsCommittedFilesAtHead() {
        val vcs = createRepository(
            vcsRevision {
                change("README.txt", "This is a test repository.")
                change("tmp.txt", "Temporary file.")
            },
            vcsRevision {
                delete("tmp.txt")
                delete("README.txt")
            },
            vcsRevision {
                change("README.txt", "This is a test repo.")
            },
            vcsRevision {
                change("gradle.properties", "org.gradle.daemon=true")
            }
        )

        assertEquals(
            expected = setOf("README.txt", "gradle.properties"),
            actual = vcs.listFiles(vcs.getHead().id)
        )
    }

    @Test
    public fun getFile_returnsContent() {
        val vcs = createRepository(
            vcsRevision {
                change("gradle.properties", "org.gradle.daemon=false")
            },
            vcsRevision {
                change("gradle.properties", "org.gradle.daemon=true")
            },
        )

        assertEquals(
            expected = "org.gradle.daemon=true",
            actual = vcs.getFile(vcs.getHead().id, path = "gradle.properties")
        )
    }

    @Test
    public fun getFile_whenFileNotFound_returnsNull() {
        val vcs = createRepository(
            vcsRevision {
                change("gradle.properties", "org.gradle.daemon=false")
            },
        )

        assertNull(vcs.getFile(vcs.getHead().id, path = "README.txt"))
    }

    @Test
    public fun getFile_whenInvalidRevision_throws() {
        val vcs = createRepository(
            vcsRevision {
                change("gradle.properties", "org.gradle.daemon=false")
            },
        )

        assertFailsWith<IllegalArgumentException> {
            vcs.getFile(revisionId = "invalid-revision", path = "gradle.properties")
        }
    }

    @Test
    public fun getHistory_whenFileNotFound_returnsEmpty() {
        val vcs = createRepository(
            vcsRevision {
                change("gradle.properties", "org.gradle.daemon=false")
            },
        )

        assertEquals(expected = emptyList(), actual = vcs.getHistory("README.txt"))
    }

    @Test
    public fun getHistory_forFile_returnsRevisionsThatTouchedFile() {
        val vcs = createRepository(
            vcsRevision {
                change("README.txt", "This is a test repository.")
                change("tmp.txt", "Temporary file.")
            },
            vcsRevision {
                delete("tmp.txt")
                delete("README.txt")
            },
            vcsRevision {
                change("README.txt", "This is a test repo.")
            },
            vcsRevision {
                change("gradle.properties", "org.gradle.daemon=true")
            },
        )

        assertEquals(expected = 3, actual = vcs.getHistory("README.txt").size)
    }

    @Test
    public fun getHistory_returnsAllRevisions() {
        val vcs = createRepository(
            vcsRevision {
                change("README.txt", "This is a test repository.")
                change("tmp.txt", "Temporary file.")
            },
            vcsRevision {
                delete("tmp.txt")
                delete("README.txt")
            },
            vcsRevision {
                change("README.txt", "This is a test repo.")
            },
            vcsRevision {
                change("gradle.properties", "org.gradle.daemon=true")
            },
        )

        val history = vcs.getHistory()

        assertEquals(expected = 4, actual = history.size)
        assertEquals(expected = vcs.getHead(), actual = history.last())
    }

    @Test
    public fun detect_whenNoRepository_returnsNull() {
        assertNull(VcsProxyFactory.detect(tmp.root))
    }

    @Test
    public fun detect_returnsEqualRepository() {
        val expectedVcs =
            createRepository(
                vcsRevision {
                    change("README.txt", "This is a test repository.")
                    change("tmp.txt", "Temporary file.")
                },
                vcsRevision {
                    delete("tmp.txt")
                    delete("README.txt")
                },
                vcsRevision {
                    change("README.txt", "This is a test repo.")
                },
                vcsRevision {
                    change("gradle.properties", "org.gradle.daemon=true")
                },
            )

        val detectedVcs = assertNotNull(VcsProxyFactory.detect(tmp.root))

        assertEquals(expected = expectedVcs.getHistory(), actual = detectedVcs.getHistory())
        for ((revisionId, _, _) in detectedVcs.getHistory()) {
            assertEquals(
                expected = expectedVcs.listFiles(revisionId),
                actual = detectedVcs.listFiles(revisionId)
            )
        }
    }
}
