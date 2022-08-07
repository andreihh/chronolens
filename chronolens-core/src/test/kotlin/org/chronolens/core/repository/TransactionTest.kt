/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.repository

import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.type
import org.chronolens.test.core.model.add
import org.chronolens.test.core.model.edit
import org.chronolens.test.core.model.qualifiedPathOf
import org.chronolens.test.core.model.remove
import org.junit.Test

class TransactionTest {
    @Test
    fun `test transaction with empty id throws`() {
        assertFailsWith<IllegalArgumentException> {
            Transaction(revisionId = "", date = Instant.now(), author = "")
        }
    }

    @Test
    fun `test transaction with invalid characters in id throws`() {
        assertFailsWith<IllegalArgumentException> {
            Transaction(revisionId = "12aA_", date = Instant.now(), author = "")
        }
    }

    @Test
    fun `test change set`() {
        val expected =
            setOf(SourcePath("Main.java"), SourcePath("Test.java"), SourcePath("MainTest.java"))
        val actual =
            Transaction(
                    revisionId = "123",
                    date = Instant.now(),
                    author = "",
                    edits =
                        listOf(
                            qualifiedPathOf("Main.java").type("Main").type("MainType").add {},
                            qualifiedPathOf("Test.java").type("Test").type("TestType").remove(),
                            qualifiedPathOf("MainTest.java").type("MainTest").edit {}
                        )
                )
                .changeSet
        assertEquals(expected, actual)
    }
}
