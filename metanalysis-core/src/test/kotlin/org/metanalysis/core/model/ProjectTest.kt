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

import org.metanalysis.core.model.SourceNode.SourceEntity.Function
import org.metanalysis.core.model.SourceNode.SourceEntity.Type
import org.metanalysis.core.model.SourceNode.SourceEntity.Variable
import org.metanalysis.core.model.SourceNode.SourceUnit
import org.metanalysis.test.core.model.addFunction
import org.metanalysis.test.core.model.addSourceUnit
import org.metanalysis.test.core.model.addType
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.editType
import org.metanalysis.test.core.model.project
import org.metanalysis.test.core.model.removeNode
import org.metanalysis.test.core.model.sourceUnit
import org.metanalysis.test.core.model.transaction
import org.metanalysis.test.core.model.variable

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.fail

class ProjectTest {
    @Test fun `test create project with duplicated unit ids throws`() {
        val unit = sourceUnit("src/Test.java") {}
        assertFailsWith<IllegalArgumentException> {
            Project(listOf(unit, unit))
        }
    }

    @Test fun `test find all returns structurally equal nodes`() {
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
                members = listOf(classVersion, classFunction)
        )
        val version = Variable(
                id = "src/Test.java:version",
                initializer = listOf("2")
        )
        val testUnit = SourceUnit(
                id = "src/Test.java",
                entities = listOf(classType, version)
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
        val actualNodes = project.findAll().toSet()

        assertEquals(expectedNodes, actualNodes)
    }

    @Test fun `test find node returns structurally equal node`() {
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
        val actualNode = project.find<Variable>("src/Test.java:IClass:version")
                ?: fail("Node '${expectedNode.id}' not found!")

        assertEquals(expectedNode, actualNode)
    }

    @Test fun `test find id with incorrect type throws`() {
        val project = project {
            sourceUnit("src/Test.java") {
                type("IClass") {}
            }
        }

        assertFailsWith<IllegalArgumentException> {
            project.find<Variable>("src/Test.java:IClass")
        }
    }

    @Test fun `test find non-existing id returns null`() {
        val project = project {
            sourceUnit("src/Test.java") {
                type("IClass") {}
                variable("version") { +"1" }
            }
        }

        assertNull(project.find<Function>("src/Test.java:getVersion()"))
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
        actual.apply(
                removeNode("src/Main.java"),
                addSourceUnit("src/Test.java") {},
                addType("src/Test.java:Test") {},
                addFunction("src/Test.java:Test:getVersion()") {},
                editType("src/Test.java:Test") {
                    modifiers { +"abstract" }
                    supertypes { +"Object" }
                }
        )

        assertEquals(expected, actual)
    }

    @Test fun `test apply transaction`() {
        val expected = project {
            sourceUnit("src/Test.java") {}
        }

        val actual = project {
            sourceUnit("src/Main.java") {}
        }
        actual.apply(transaction("123") {
            removeNode("src/Main.java")
            addSourceUnit("src/Test.java") {}
        })

        assertEquals(expected, actual)
    }
}
