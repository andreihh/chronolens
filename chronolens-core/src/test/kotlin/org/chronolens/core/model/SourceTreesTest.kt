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
import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.function
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
    fun `test source tree returns structurally equal nodes`() {
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

        for (id in sourceTree.walk().map(SourceTreeNode<*>::qualifiedId)) {
            assertTrue(id in sourceTree)
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

        assertFalse("src/Test.java:IClass#VERSION" in sourceTree)
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
        val nodeId = sourceFile("src/Test.java").type("IClass").variable("version").id()
        val actualNode = sourceTree.get<Variable>(nodeId)

        assertEquals(expectedNode, actualNode)
    }

    @Test
    fun getNode_withIncorrectType_throws() {
        val sourceTree = sourceTree { +sourceFile("src/Test.java") { +type("IClass") {} } }
        val nodeId = sourceFile("src/Test.java").type("IClass").id()

        assertFailsWith<IllegalStateException> { sourceTree.get<Variable>(nodeId) }
    }

    @Test
    fun getNode_nonExistingId_throws() {
        val sourceTree = sourceTree {
            +sourceFile("src/Test.java") {
                +type("IClass") {}
                +variable("version") { +"1" }
            }
        }
        val nodeId = sourceFile("src/Test.java").function("getVersion()").id()

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
        val nodeId = sourceFile("src/Test.java").function("getVersion()").id()

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
            sourceFile("src/Main.java").remove(),
            sourceFile("src/Test.java").add {},
            sourceFile("src/Test.java").type("Test").add {},
            sourceFile("src/Test.java").type("Test").function("getVersion()").add {},
            sourceFile("src/Test.java").type("Test").edit {
                modifiers { +"abstract" }
                supertypes { +"Object" }
            }
        )

        assertEquals(expected, actual)
    }

    @Test
    fun sourcePath_ofSourceFile_returnsPath() {
        val path = SourcePath("src/Test.java")
        val source = SourceFile(path)
        val sourceTreeNode = SourceTreeNode.of(source)

        assertEquals(path, sourceTreeNode.sourcePath)
    }

    @Test
    fun sourcePath_ofNode_returnsPathOfContainerSourceFile() {
        val path = "src/Test.java"
        val qualifiedId = "$path#getVersion(String)"
        val node = sourceFile(path) { +function("getVersion(String)") {} }
        val sourceTreeNode = SourceTreeNode(qualifiedId, node)

        assertEquals(path, sourceTreeNode.sourcePath.toString())
    }
}
