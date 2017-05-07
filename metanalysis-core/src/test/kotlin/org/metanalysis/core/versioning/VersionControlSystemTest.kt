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

package org.metanalysis.core.versioning

import org.junit.Test

import org.metanalysis.test.core.versioning.VersionControlSystemMock

import kotlin.test.assertNull
import kotlin.test.assertTrue

class VersionControlSystemTest {
    class UnsupportedVersionControlSystem : VersionControlSystem() {
        override fun isSupported(): Boolean = false

        override fun detectRepository(): Boolean = false

        override fun getHead(): Revision = TODO("not implemented")

        override fun getRawRevision(revisionId: String): String =
            TODO("not implemented")

        override fun listFiles(revision: Revision): Set<String> =
                TODO("not implemented")

        override fun getFile(revision: Revision, path: String): String? =
                TODO("not implemented")

        override fun getFileHistory(
                revision: Revision,
                path: String
        ): List<Revision> = TODO("not implemented")
    }

    @Test fun `test detect vcs ignores unsupported or undetected`() {
        val vcs = VersionControlSystem.detect()
        assertNull(vcs)
    }

    @Test fun `test detect initialized vcs returns non-null`() {
        VersionControlSystemMock.setRepository(emptyList())
        val vcs = VersionControlSystem.detect()
        assertTrue(vcs is VersionControlSystemMock)
    }
}
