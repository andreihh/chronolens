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

import org.chronos.core.Node.Variable
import org.chronos.test.assertEquals

import org.junit.Test

class VariableChangeTest {
    @Test fun `test variable change initializer from null to not-null`() {
        val name = "version"
        val initializer = "1"
        val expected = Variable(name, initializer)
        val actual = Variable(name, null).apply(VariableChange(initializer))
        assertEquals(expected, actual)
    }

    @Test fun `test variable change initializer from not-null to null`() {
        val name = "version"
        val initializer = "1"
        val expected = Variable(name, null)
        val actual = Variable(name, initializer).apply(VariableChange(null))
        assertEquals(expected, actual)
    }

    @Test fun `test variable change initializer from not-null to not-null`() {
        val name = "version"
        val initializer = "1"
        val expectedInitializer = "2"
        val expected = Variable(name, expectedInitializer)
        val actual = Variable(name, initializer)
                .apply(VariableChange(expectedInitializer))
        assertEquals(expected, actual)
    }
}
