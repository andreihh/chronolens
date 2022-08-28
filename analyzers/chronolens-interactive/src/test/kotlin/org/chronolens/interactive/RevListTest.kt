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

package org.chronolens.interactive

import kotlin.test.Test
import kotlin.test.assertEquals
import org.chronolens.test.core.analysis.OptionsProviderBuilder
import org.chronolens.test.core.model.revision
import org.chronolens.test.core.repository.repository
import org.chronolens.test.core.repository.revisionListOf

class RevListTest {
    private fun create() = RevListSpec().create(OptionsProviderBuilder().build())

    @Test
    fun analyze_returnsAllRevisions() {
        val repository = repository {
            +revision("1") {}
            +revision("2") {}
            +revision("a") {}
            +revision("b") {}
        }

        assertEquals(
            expected = revisionListOf("1", "2", "a", "b"),
            actual = create().analyze(repository).revisions
        )
    }

    @Test
    fun reportToString_listsOneRevisionPerLine() {
        val expected =
            """
            12
            34
            ab
            cd
        """.trimIndent()

        val actual = RevListReport(revisionListOf("12", "34", "ab", "cd")).toString()

        assertEquals(expected, actual)
    }
}
