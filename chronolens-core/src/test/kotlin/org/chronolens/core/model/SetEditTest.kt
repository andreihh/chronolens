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

package org.chronolens.core.model

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.chronolens.core.model.SetEdit.Add
import org.chronolens.core.model.SetEdit.Companion.apply
import org.chronolens.core.model.SetEdit.Remove
import org.junit.Test

class SetEditTest {
    @Test
    fun `test add non-existing element`() {
        val expected = setOf(1, 2)
        val actual = setOf(1).apply(Add(2))
        assertEquals(expected, actual)
    }

    @Test
    fun `test add duplicate throws`() {
        assertFailsWith<IllegalStateException> { setOf(1).apply(Add(1)) }
    }

    @Test
    fun `test remove existing element`() {
        val expected = setOf(1)
        val actual = setOf(1, 2).apply(Remove(2))
        assertEquals(expected, actual)
    }

    @Test
    fun `test remove non-existing element throws`() {
        assertFailsWith<IllegalStateException> { setOf(1).apply(Remove(2)) }
    }

    @Test
    fun `test chained edits`() {
        val expected = setOf(1, 2, 3, 4, 6, 7)
        val actual =
            setOf(1, 4).apply(Remove(4), Add(2), Add(3), Add(4), Add(5), Add(6), Add(7), Remove(5))
        assertEquals(expected, actual)
    }
}
