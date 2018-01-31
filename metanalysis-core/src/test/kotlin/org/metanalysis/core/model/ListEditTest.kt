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
import org.metanalysis.core.model.ListEdit.Add
import org.metanalysis.core.model.ListEdit.Companion.apply
import org.metanalysis.core.model.ListEdit.Companion.diff
import org.metanalysis.core.model.ListEdit.Remove
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ListEditTest {
    private fun String.apply(edits: List<ListEdit<Char>>): String =
        toList().apply(edits).joinToString(separator = "")

    private fun String.apply(vararg edits: ListEdit<Char>): String =
        toList().apply(*edits).joinToString(separator = "")

    private fun assertDiff(src: String, dst: String) {
        assertEquals(dst, src.apply(src.toList().diff(dst.toList())))
        assertEquals(src, dst.apply(dst.toList().diff(src.toList())))
    }

    @Test fun `test add element at negative position throws`() {
        assertFailsWith<IllegalArgumentException> {
            Add(index = -1, value = '0')
        }
    }

    @Test fun `test add element at end`() {
        val expected = "12345"
        val actual = "1234".apply(Add(index = 4, value = '5'))
        assertEquals(expected, actual)
    }

    @Test fun `test add element before end`() {
        val expected = "12345"
        val actual = "1235".apply(Add(index = 3, value = '4'))
        assertEquals(expected, actual)
    }

    @Test fun `test add element at front`() {
        val expected = "12345"
        val actual = "2345".apply(Add(index = 0, value = '1'))
        assertEquals(expected, actual)
    }

    @Test fun `test add element in middle`() {
        val expected = "12345"
        val actual = "1245".apply(Add(index = 2, value = '3'))
        assertEquals(expected, actual)
    }

    @Test fun `test add element out of bounds throws`() {
        assertFailsWith<IllegalStateException> {
            "1234".apply(Add(index = 5, value = '6'))
        }
    }

    @Test fun `test remove element at negative index throws`() {
        assertFailsWith<IllegalArgumentException> {
            Remove<Char>(-1)
        }
    }

    @Test fun `test remove first element`() {
        val expected = "2345"
        val actual = "12345".apply(Remove(0))
        assertEquals(expected, actual)
    }

    @Test fun `test remove last element`() {
        val expected = "1234"
        val actual = "12345".apply(Remove(4))
        assertEquals(expected, actual)
    }

    @Test fun `test remove middle element`() {
        val expected = "1245"
        val actual = "12345".apply(Remove(2))
        assertEquals(expected, actual)
    }

    @Test fun `test remove element out of bounds throws`() {
        assertFailsWith<IllegalStateException> {
            "1234".apply(Remove(4))
        }
    }

    @Test fun `test chained edits`() {
        val expected = "republican"
        val actual = "democrats".apply(
            Remove(8), // democrat
            Remove(7), // democra
            Add(index = 7, value = 'n'), // democran
            Remove(5), // democan
            Add(index = 5, value = 'c'), // democcan
            Remove(4), // democan
            Add(index = 4, value = 'i'), // demoican
            Remove(3), // demican
            Add(index = 3, value = 'l'), // demlican
            Remove(2), // delican
            Add(index = 2, value = 'b'), // deblican
            Add(index = 2, value = 'u'), // deublican
            Add(index = 2, value = 'p'), // depublican
            Remove(0), // epublican
            Add(index = 0, value = 'r') // republican
        )
        assertEquals(expected, actual)
    }

    @Test fun `test diff`() {
        val src = "democrats"
        val dst = "republican"
        assertDiff(src, dst)
    }

    @Test fun `test diff with empty list`() {
        val src = "democrats"
        val dst = ""
        assertDiff(src, dst)
    }
}
