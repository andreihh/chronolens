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

package org.chronos.core.delta

import org.chronos.core.delta.MapEdit.Companion.apply
import org.chronos.core.delta.MapEdit.Companion.diff

import org.junit.Test

import kotlin.test.assertEquals

class MapEditTest {
    private fun <K, V : Any> assertDiff(src: Map<K, V>, dst: Map<K, V>) {
        assertEquals(dst, src.apply(src.diff(dst)))
        assertEquals(src, dst.apply(dst.diff(src)))
    }

    @Test fun `test add non-existing key`() {
        val expected = mapOf(1 to "a", 2 to "b")
        val actual = mapOf(1 to "a").apply(MapEdit.Add(2, "b"))
        assertEquals(expected, actual)
    }

    @Test(expected = IllegalStateException::class)
    fun `test add duplicate key throws`() {
        mapOf(1 to "a").apply(MapEdit.Add(1, "a"))
    }

    @Test fun `test remove existing key`() {
        val expected = mapOf(1 to "a")
        val actual = mapOf(1 to "a", 2 to "b").apply(MapEdit.Remove(2))
        assertEquals(expected, actual)
    }

    @Test(expected = IllegalStateException::class)
    fun `test remove non-existing key throws`() {
        mapOf(1 to "a").apply(MapEdit.Remove(2))
    }

    @Test fun `test replace key value`() {
        val expected = mapOf(1 to "A", 2 to "b")
        val actual = mapOf(1 to "a", 2 to "b").apply(MapEdit.Replace(1, "A"))
        assertEquals(expected, actual)
    }

    @Test(expected = IllegalStateException::class)
    fun `test replace non-existing key throws`() {
        mapOf(2 to "b").apply(MapEdit.Replace(1, "A"))
    }

    @Test fun `test chained edits`() {
        val expected = mapOf(1 to "a", 2 to "b", 4 to "d", 5 to "e")
        val actual = mapOf(1 to "a", 4 to "d").apply(
                MapEdit.Remove(4),
                MapEdit.Add(2, "b"),
                MapEdit.Add(3, "c"),
                MapEdit.Add(4, "d"),
                MapEdit.Add(5, "e"),
                MapEdit.Remove(3)
        )
        assertEquals(expected, actual)
    }

    @Test fun `test diff`() {
        val src = mapOf(1 to "a", 2 to "b", 5 to "e", 6 to "f")
        val dst = mapOf(1 to "A", 3 to "c", 4 to "D", 6 to "f")
        assertDiff(src, dst)
    }
}
