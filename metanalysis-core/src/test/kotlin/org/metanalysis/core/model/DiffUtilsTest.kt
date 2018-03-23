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
import org.metanalysis.test.core.model.sourceFile
import org.metanalysis.test.core.model.type
import org.metanalysis.test.core.model.variable
import kotlin.test.assertFailsWith

class DiffUtilsTest {
    @Test fun `test diff nodes with different ids throws`() {
        val before = sourceFile("src/Test.java") {}
        val after = sourceFile("src/Main.java") {}
        assertFailsWith<IllegalArgumentException> {
            before.diff(after)
        }
    }

    @Test fun `test diff nodes of different types throws`() {
        val before = type("src/Test.java:Test") {}
        val after = variable("src/Test.java:Test") {}
        assertFailsWith<IllegalArgumentException> {
            before.diff(after)
        }
    }
}
