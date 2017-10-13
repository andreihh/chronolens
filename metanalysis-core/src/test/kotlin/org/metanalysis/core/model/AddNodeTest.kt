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

import org.metanalysis.core.model.ProjectEdit.AddNode
import org.metanalysis.core.model.SourceNode.SourceEntity.Function
import org.metanalysis.core.model.SourceNode.SourceEntity.Type
import org.metanalysis.core.model.SourceNode.SourceEntity.Variable
import org.metanalysis.core.model.SourceNode.SourceUnit
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.project

import kotlin.test.assertFailsWith

class AddNodeTest {
    @Test fun `test add source unit`() {
        val expected = project {
            sourceUnit("src/Main.java") {}
            sourceUnit("src/Test.java") {}
        }

        val actual = project {
            sourceUnit("src/Main.java") {}
        }
        actual.apply(AddNode(SourceUnit("src/Test.java")))

        assertEquals(expected, actual)
    }

    @Test fun `test add parameter to method`() {
        val expected = project {
            sourceUnit("src/Test.java") {
                type("Test") {
                    function("getVersion()") {
                        parameter("name") {}
                    }
                }
            }
        }

        val actual = project {
            sourceUnit("src/Test.java") {
                type("Test") {
                    function("getVersion()") {}
                }
            }
        }
        actual.apply(AddNode(Variable("src/Test.java:Test:getVersion():name")))

        assertEquals(expected, actual)
    }

    @Test fun `test add function to type`() {
        val expected = project {
            sourceUnit("src/Test.java") {
                type("Test") {
                    function("getVersion()") {}
                }
            }
        }

        val actual = project {
            sourceUnit("src/Test.java") {
                type("Test") {}
            }
        }
        actual.apply(AddNode(Function("src/Test.java:Test:getVersion()")))

        assertEquals(expected, actual)
    }

    @Test fun `test add type with method with parameter`() {
        val expected = project {
            sourceUnit("src/Test.java") {
                type("Test") {
                    function("getV(String)") {
                        parameter("name") {}
                    }
                }
            }
        }

        val actual = project {
            sourceUnit("src/Test.java") {}
        }
        actual.apply(AddNode(Type(
                id = "src/Test.java:Test",
                members = listOf(Function(
                        id = "src/Test.java:Test:getV(String)",
                        parameters = listOf(Variable(
                                id = "src/Test.java:Test:getV(String):name"
                        ))
                ))
        )))

        assertEquals(expected, actual)
    }

    @Test fun `test add existing node throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                type("Test") {}
            }
        }
        val edit = AddNode(Type("src/Test.java:Test"))

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }

    @Test fun `test add parameter to non-existing function throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                function("getVersion()") {}
            }
        }
        val edit = AddNode(Variable("src/Test.java:getV():name"))

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }

    @Test fun `test add entity to variable throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                variable("version") {}
            }
        }
        val edit = AddNode(Function("src/Test.java:version:getVersion()"))

        assertFailsWith<IllegalStateException> {
            project.apply(edit)
        }
    }
}
