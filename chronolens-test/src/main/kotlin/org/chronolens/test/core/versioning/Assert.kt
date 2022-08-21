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
import kotlin.test.assertEquals

public fun assertEqualVcsProxies(
    expected: VcsProxy?,
    actual: VcsProxy?,
    message: String? = null,
) {
    assertEquals(expected?.getHead(), actual?.getHead(), message)
    assertEquals(expected?.getHistory(), actual?.getHistory(), message)
    for (revision in expected?.getHistory().orEmpty()) {
        assertEquals(
            expected = expected?.getChangeSet(revision.id),
            actual = actual?.getChangeSet(revision.id),
            message = message
        )
        assertEquals(
            expected = expected?.listFiles(revision.id),
            actual = actual?.listFiles(revision.id),
            message = message
        )
        for (path in expected?.listFiles(revision.id).orEmpty()) {
            assertEquals(
                expected = expected?.getFile(revision.id, path),
                actual = actual?.getFile(revision.id, path),
                message = message
            )
        }
    }
}
