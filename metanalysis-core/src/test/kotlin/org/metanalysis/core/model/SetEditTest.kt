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

package org.metanalysis.core.model

import org.junit.Test

import org.metanalysis.core.model.SetEdit.Add
import org.metanalysis.core.model.SetEdit.Companion.apply
import org.metanalysis.core.model.SetEdit.Companion.diff
import org.metanalysis.core.model.SetEdit.Remove

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SetEditTest {
    private fun <T> assertDiff(src: Set<T>, dst: Set<T>) {
        assertEquals(dst, src.apply(src.diff(dst)))
        assertEquals(src, dst.apply(dst.diff(src)))
    }

    @Test fun `test add non-existing element`() {
        val expected = setOf(1, 2)
        val actual = setOf(1).apply(SetEdit.Add(2))
        assertEquals(expected, actual)
    }

    @Test fun `test add duplicate throws`() {
        assertFailsWith<IllegalStateException> {
            setOf(1).apply(Add(1))
        }
    }

    @Test fun `test remove existing element`() {
        val expected = setOf(1)
        val actual = setOf(1, 2).apply(Remove(2))
        assertEquals(expected, actual)
    }

    @Test fun `test remove non-existing element throws`() {
        assertFailsWith<IllegalStateException> {
            setOf(1).apply(SetEdit.Remove(2))
        }
    }

    @Test fun `test chained edits`() {
        val expected = setOf(1, 2, 3, 4, 6, 7)
        val actual = setOf(1, 4).apply(
                Remove(4),
                Add(2),
                Add(3),
                Add(4),
                Add(5),
                Add(6),
                Add(7),
                Remove(5)
        )
        assertEquals(expected, actual)
    }

    @Test fun `test diff`() {
        val src = setOf(1, 2, 5, 6)
        val dst = setOf(1, 3, 4, 6)
        assertDiff(src, dst)
    }
}
