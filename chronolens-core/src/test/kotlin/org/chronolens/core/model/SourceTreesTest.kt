/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.model

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.chronolens.core.model.SourceTreeEdit.Companion.apply
import org.chronolens.test.core.model.add
import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.edit
import org.chronolens.test.core.model.function
import org.chronolens.test.core.model.remove
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.model.sourceTree
import org.chronolens.test.core.model.type
import org.chronolens.test.core.model.variable
import org.junit.Test

class SourceTreeTest {
    @Test
    fun createSourceTree_withDuplicatedSourcePaths_throws() {
        val source = sourceFile("src/Test.java") {}
        assertFailsWith<IllegalArgumentException> { SourceTree.of(listOf(source, source)) }
    }

    @Test
    fun walk_returnsStructurallyEqualNodes() {
        val classVersion = variable("version") { +"1" }
        val classFunction = function("getVersion()") {}
        val classType =
            type("IClass") {
                supertypes("Object")
                modifiers("interface")
                +classVersion
                +classFunction
            }
        val version = variable("version") { +"2" }
        val testSource =
            sourceFile("src/Test.java") {
                +classType
                +version
            }
        val expectedNodes = setOf(testSource, version, classType, classVersion, classFunction)

        val sourceTree = sourceTree {
            +sourceFile("src/Test.java") {
                +type("IClass") {
                    modifiers("interface")
                    supertypes("Object")
                    +variable("version") { +"1" }
                    +function("getVersion()") {}
                }
                +variable("version") { +"2" }
            }
        }
        val actualNodes = sourceTree.walk().map(SourceTreeNode<*>::sourceNode).toSet()

        assertEquals(expectedNodes, actualNodes)
    }

    @Test
    fun contains_existingNode_returnsTrue() {
        val sourceTree = sourceTree {
            +sourceFile("src/Test.java") {
                +type("IClass") {
                    modifiers("interface")
                    supertypes("Object")
                    +variable("version") { +"1" }
                    +function("getVersion()") {}
                }
                +variable("version") { +"2" }
            }
        }

        for (qualifiedId in sourceTree.walk().map(SourceTreeNode<*>::qualifiedId)) {
            assertTrue(qualifiedId in sourceTree)
        }
    }

    @Test
    fun contains_nonExistingNode_returnsFalse() {
        val sourceTree = sourceTree {
            +sourceFile("src/Test.java") {
                +type("IClass") {
                    modifiers("interface")
                    supertypes("Object")
                    +variable("version") { +"1" }
                    +function("getVersion()") {}
                }
                +variable("version") { +"2" }
            }
        }
        val qualifiedId = qualifiedPathOf("src/Test.java").type("IClass").variable("VERSION")

        assertFalse(qualifiedId in sourceTree)
    }

    @Test
    fun getNode_returnsStructurallyEqualNode() {
        val expectedNode = variable("version") { +"1" }

        val sourceTree = sourceTree {
            +sourceFile("src/Test.java") {
                +type("IClass") {
                    modifiers("interface")
                    supertypes("Object")
                    +variable("version") { +"1" }
                    +function("getVersion()") {}
                }
                +variable("version") { +"2" }
            }
        }
        val nodeId = qualifiedPathOf("src/Test.java").type("IClass").variable("version")
        val actualNode = sourceTree.get<Variable>(nodeId)

        assertEquals(expectedNode, actualNode)
    }

    // TODO: check how to replace this test.
    /*@Test
    fun getNode_withIncorrectType_throws() {
        val sourceTree = sourceTree { +sourceFile("src/Test.java") { +type("IClass") {} } }
        val nodeId = qualifiedPathOf("src/Test.java").type("IClass")

        assertFailsWith<IllegalStateException> { sourceTree.get<Variable>(nodeId) }
    }*/

    @Test
    fun getNode_nonExistingId_throws() {
        val sourceTree = sourceTree {
            +sourceFile("src/Test.java") {
                +type("IClass") {}
                +variable("version") { +"1" }
            }
        }
        val nodeId = qualifiedPathOf("src/Test.java").function("getVersion()")

        assertFailsWith<IllegalStateException> { sourceTree.get<Function>(nodeId) }
    }

    @Test
    fun get_nonExistingId_returnsNull() {
        val sourceTree = sourceTree {
            +sourceFile("src/Test.java") {
                +type("IClass") {}
                +variable("version") { +"1" }
            }
        }
        val nodeId = qualifiedPathOf("src/Test.java").function("getVersion()")

        assertNull(sourceTree[nodeId])
    }

    @Test
    fun apply_chainedEdits_performsAllChanges() {
        val expected = sourceTree {
            +sourceFile("src/Test.java") {
                +type("Test") {
                    modifiers("abstract")
                    supertypes("Object")
                    +function("getVersion()") {}
                }
            }
        }

        val actual = sourceTree { +sourceFile("src/Main.java") {} }
        actual.apply(
            qualifiedPathOf("src/Main.java").remove(),
            qualifiedPathOf("src/Test.java").add {},
            qualifiedPathOf("src/Test.java").type("Test").add {},
            qualifiedPathOf("src/Test.java").type("Test").function("getVersion()").add {},
            qualifiedPathOf("src/Test.java").type("Test").edit {
                modifiers { +"abstract" }
                supertypes { +"Object" }
            }
        )

        assertEquals(expected, actual)
    }
}
