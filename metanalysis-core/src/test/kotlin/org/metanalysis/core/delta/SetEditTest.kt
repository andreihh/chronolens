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

package org.metanalysis.core.delta

import org.junit.Test

import org.metanalysis.core.delta.SetEdit.Companion.apply
import org.metanalysis.core.delta.SetEdit.Companion.diff

import kotlin.test.assertEquals

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

    @Test(expected = IllegalStateException::class)
    fun `test add duplicate throws`() {
        setOf(1).apply(SetEdit.Add(1))
    }

    @Test fun `test remove existing element`() {
        val expected = setOf(1)
        val actual = setOf(1, 2).apply(SetEdit.Remove(2))
        assertEquals(expected, actual)
    }

    @Test(expected = IllegalStateException::class)
    fun `test remove non-existing element throws`() {
        setOf(1).apply(SetEdit.Remove(2))
    }

    @Test fun `test chained edits`() {
        val expected = setOf(1, 2, 3, 4, 6, 7)
        val actual = setOf(1, 4).apply(
                SetEdit.Remove(4),
                SetEdit.Add(2),
                SetEdit.Add(3),
                SetEdit.Add(4),
                SetEdit.Add(5),
                SetEdit.Add(6),
                SetEdit.Add(7),
                SetEdit.Remove(5)
        )
        assertEquals(expected, actual)
    }

    @Test fun `test diff`() {
        val src = setOf(1, 2, 5, 6)
        val dst = setOf(1, 3, 4, 6)
        assertDiff(src, dst)
    }
}
