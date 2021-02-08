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

package org.chronolens.core.repository

import org.chronolens.core.model.AddNode
import org.chronolens.core.model.EditType
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.model.Type
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransactionTest {
    @Test fun `test transaction with empty id throws`() {
        assertFailsWith<IllegalArgumentException> {
            Transaction(revisionId = "", date = Instant.now(), author = "")
        }
    }

    @Test fun `test transaction with invalid characters in id throws`() {
        assertFailsWith<IllegalArgumentException> {
            Transaction(revisionId = "12aA_", date = Instant.now(), author = "")
        }
    }

    @Test fun `test change set`() {
        val expected = setOf("Main.java", "Test.java", "MainTest.java")
        val actual = Transaction(
            revisionId = "123",
            date = Instant.now(),
            author = "",
            edits = listOf(
                AddNode(
                    id = "Main.java:Main:MainType",
                    node = Type("Main.java:Main:MainType"),
                ),
                RemoveNode("Test.java:Test:TestType"),
                EditType("MainTest.java:MainTest")
            )
        ).changeSet
        assertEquals(expected, actual)
    }
}
