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

package org.metanalysis.core.repository

import org.junit.Test
import kotlin.test.assertFailsWith

class TransactionTest {
    @Test fun `test transaction with empty id throws`() {
        assertFailsWith<IllegalArgumentException> {
            Transaction(id = "", date = 1L, author = "<author>")
        }
    }

    @Test fun `test transaction with invalid characters in id throws`() {
        assertFailsWith<IllegalArgumentException> {
            Transaction(id = "123aA_", date = 1L, author = "<author>")
        }
    }

    @Test fun `test transaction with negative date throws`() {
        assertFailsWith<IllegalArgumentException> {
            Transaction(id = "123", date = -1L, author = "<author>")
        }
    }
}
