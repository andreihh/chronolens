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

import org.chronos.core.delta.TypeChange.SupertypeChange

import org.junit.Test

import kotlin.test.assertEquals

class SupertypeChangeTest {
    @Test fun `test add supertype`() {
        val expected = setOf("IInterface", "IClass")
        val actual = setOf("IInterface").apply(SupertypeChange.Add("IClass"))
        assertEquals(expected, actual)
    }

    @Test fun `test remove supertype`() {
        val expected = setOf("IInterface")
        val actual = setOf("IInterface", "IClass")
                .apply(SupertypeChange.Remove("IClass"))
        assertEquals(expected, actual)
    }
}
