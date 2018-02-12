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
import org.metanalysis.test.core.model.addFunction
import org.metanalysis.test.core.model.addSourceUnit
import org.metanalysis.test.core.model.addType
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.editType
import org.metanalysis.test.core.model.project
import org.metanalysis.test.core.model.removeNode
import org.metanalysis.test.core.model.sourceUnit
import org.metanalysis.test.core.model.variable
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ProjectTest {
    @Test fun `test create project with duplicated unit ids throws`() {
        val unit = sourceUnit("src/Test.java") {}
        assertFailsWith<IllegalArgumentException> {
            Project.of(listOf(unit, unit))
        }
    }

    @Test fun `test source tree returns structurally equal nodes`() {
        val classVersion = Variable(
            id = "src/Test.java:IClass:version",
            initializer = listOf("1")
        )
        val classFunction = Function(
            id = "src/Test.java:IClass:getVersion()"
        )
        val classType = Type(
            id = "src/Test.java:IClass",
            modifiers = setOf("interface"),
            supertypes = setOf("Object"),
            members = setOf(classVersion, classFunction)
        )
        val version = Variable(
            id = "src/Test.java:version",
            initializer = listOf("2")
        )
        val testUnit = SourceUnit(
            id = "src/Test.java",
            entities = setOf(classType, version)
        )
        val expectedNodes =
            setOf(testUnit, version, classType, classVersion, classFunction)

        val project = project {
            sourceUnit("src/Test.java") {
                type("IClass") {
                    modifiers("interface")
                    supertypes("Object")
                    variable("version") { +"1" }
                    function("getVersion()") {}
                }
                variable("version") { +"2" }
            }
        }
        val actualNodes = project.sourceTree.toSet()

        assertEquals(expectedNodes, actualNodes)
    }

    @Test fun `test get node returns structurally equal node`() {
        val expectedNode =
            variable("src/Test.java:IClass:version") { +"1" }

        val project = project {
            sourceUnit("src/Test.java") {
                type("IClass") {
                    modifiers("interface")
                    supertypes("Object")
                    variable("version") { +"1" }
                    function("getVersion()") {}
                }
                variable("version") { +"2" }
            }
        }
        val actualNode = project.get<Variable>("src/Test.java:IClass:version")

        assertEquals(expectedNode, actualNode)
    }

    @Test fun `test get id with incorrect type throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                type("IClass") {}
            }
        }

        assertFailsWith<IllegalStateException> {
            project.get<Variable>("src/Test.java:IClass")
        }
    }

    @Test fun `test get non-existing id throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                type("IClass") {}
                variable("version") { +"1" }
            }
        }

        assertFailsWith<IllegalStateException> {
            project.get<Function>("src/Test.java:getVersion()")
        }
    }

    @Test fun `test get non-existing id returns null`() {
        val project = project {
            sourceUnit("src/Test.java") {
                type("IClass") {}
                variable("version") { +"1" }
            }
        }

        assertNull(project.get<Function?>("src/Test.java:getVersion()"))
    }

    @Test fun `test apply chained edits`() {
        val expected = project {
            sourceUnit("src/Test.java") {
                type("Test") {
                    modifiers("abstract")
                    supertypes("Object")
                    function("getVersion()") {}
                }
            }
        }

        val actual = project {
            sourceUnit("src/Main.java") {}
        }
        actual.apply(listOf(
            removeNode("src/Main.java"),
            addSourceUnit("src/Test.java") {},
            addType("src/Test.java:Test") {},
            addFunction("src/Test.java:Test:getVersion()") {},
            editType("src/Test.java:Test") {
                modifiers { +"abstract" }
                supertypes { +"Object" }
            }
        ))

        assertEquals(expected, actual)
    }
}
