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

import org.chronos.core.Node.Type
import org.chronos.core.delta.Change.Companion.apply
import org.chronos.core.delta.TypeChange.SupertypeChange
import org.chronos.test.assertEquals

import org.junit.Test

class TypeChangeTest {
    @Test fun `test add supertype`() {
        val name = "IClass"
        val supertype = "IInterface"
        val expected = Type(name, setOf(supertype))
        val actual = Type(name).apply(TypeChange(
                supertypeChanges = listOf(SupertypeChange.Add(supertype)),
                memberChanges = emptyList()
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test remove supertype`() {
        val name = "IClass"
        val supertype = "IInterface"
        val expected = Type(name)
        val actual = Type(name, setOf(supertype)).apply(TypeChange(
                supertypeChanges = listOf(SupertypeChange.Remove(supertype)),
                memberChanges = emptyList()
        ))
        assertEquals(expected, actual)
    }

    /*@Test fun `test add type`() {
        TODO()
    }

    @Test fun `test add variable`() {
        TODO()
    }

    @Test fun `test add function`() {
        TODO()
    }

    @Test fun `test remove type`() {
        TODO()
    }

    @Test fun `test remove variable`() {
        TODO()
    }

    @Test fun `test remove function`() {
        TODO()
    }

    @Test fun `test change type`() {
        TODO()
    }

    @Test fun `test change variable`() {
        TODO()
    }

    @Test fun `test change function`() {
        TODO()
    }*/
}
