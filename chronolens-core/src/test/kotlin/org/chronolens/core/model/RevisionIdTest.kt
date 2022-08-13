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

package org.chronolens.core.model

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.Test

class RevisionIdTest {
    @Test
    fun newRevisionId_whenValid_isCreatedSuccessfully() {
        RevisionId("abcDEF123_-")
    }

    @Test
    fun newRevisionId_whenInvalid_throws() {
        assertFailsWith<IllegalArgumentException> { RevisionId("abcDEF123_-:") }
    }

    @Test
    fun toString_returnsRawId() {
        val rawId = "abcDEF123_-"
        assertEquals(RevisionId(rawId).toString(), rawId)
    }
}
