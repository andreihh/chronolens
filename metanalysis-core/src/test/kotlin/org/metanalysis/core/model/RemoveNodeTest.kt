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
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.project
import org.metanalysis.test.core.model.removeNode
import kotlin.test.assertFailsWith

class RemoveNodeTest {
    @Test fun `test remove invalid id throws`() {
        assertFailsWith<IllegalArgumentException> {
            RemoveNode("src/Test.java:/")
        }
    }

    @Test fun `test remove source unit`() {
        val expected = project {
            sourceUnit("src/Test.java") {}
        }

        val actual = project {
            sourceUnit("src/Main.java") {
                type("Main") {
                    function("getVersion(String)") {
                        parameters("name")
                    }
                }
            }
            sourceUnit("src/Test.java") {}
        }
        actual.apply(removeNode("src/Main.java"))

        assertEquals(expected, actual)
    }

    @Test fun `test remove function from type`() {
        val expected = project {
            sourceUnit("src/Main.java") {
                type("Main") {
                    variable("version") {}
                }
            }
        }

        val actual = project {
            sourceUnit("src/Main.java") {
                type("Main") {
                    variable("version") {}
                    function("getVersion(String)") {
                        parameters("name")
                    }
                }
            }
        }
        actual.apply(removeNode("src/Main.java:Main:getVersion(String)"))

        assertEquals(expected, actual)
    }

    @Test fun `test remove non-existing node throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                function("getVersion()") {}
            }
        }
        val edit = removeNode("src/Test.java:version")

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }
}
