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
import org.metanalysis.test.core.model.function
import org.metanalysis.test.core.model.sourceUnit
import org.metanalysis.test.core.model.type
import org.metanalysis.test.core.model.variable
import kotlin.test.assertEquals

class UtilsTest {
    @Test fun `test children of unit are equal to entities`() {
        val unit = sourceUnit("src/Test.java") {
            type("Test") {}
        }
        assertEquals(unit.entities, unit.children)
    }

    @Test fun `test children of type are equal to members`() {
        val type = type("src/Test.java:Test") {
            variable("version") {}
        }
        assertEquals(type.members, type.children)
    }

    @Test fun `test children of function are equal to parameters`() {
        val function = function("src/Test.java:getVersion(String)") {
            parameter("name") {}
        }
        assertEquals(function.parameters, function.children)
    }

    @Test fun `test children of variable are equal to empty collection`() {
        val variable = variable("src/Test.java:VERSION") {}
        assertEquals(emptyList(), variable.children.toList())
    }

    @Test fun `test source path of unit`() {
        val unit = sourceUnit("src/Test.java") {}
        assertEquals(unit.path, unit.sourcePath)
    }

    @Test fun `test source path of node`() {
        val path = "src/Test.java"
        val node = variable("$path:getVersion(String):name") {}
        assertEquals(path, node.sourcePath)
    }
}
