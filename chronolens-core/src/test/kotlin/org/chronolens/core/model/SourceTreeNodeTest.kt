/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import kotlin.test.assertNull
import org.chronolens.test.core.model.build
import org.chronolens.test.core.model.function
import org.chronolens.test.core.model.type
import org.chronolens.test.core.model.variable
import org.junit.Test

class SourceTreeNodeTest {
    @Test
    fun newSourceTreeNode_ofMismatchingKinds_throws() {
        val qualifiedId = qualifiedSourcePathOf("src/Main.java").type("Main")
        val sourceNode = variable("Main") {}

        assertFailsWith<IllegalArgumentException> {
            SourceTreeNode(qualifiedId, sourceNode)
        }
    }

    @Test
    fun newSourceTreeNode_ofMismatchingIds_throws() {
        val qualifiedId = qualifiedSourcePathOf("src/Main.java").type("Main")
        val sourceNode = type("main") {}

        assertFailsWith<IllegalArgumentException> {
            SourceTreeNode(qualifiedId, sourceNode)
        }
    }

    @Test
    fun kind_isEqualToQualifiedIdAndSourceNodeKind() {
        val qualifiedId = qualifiedSourcePathOf("src/Main.java").type("Main")
        val sourceNode = type("Main") {}
        val sourceTreeNode = SourceTreeNode(qualifiedId, sourceNode)

        assertEquals(expected = qualifiedId.kind, actual = sourceTreeNode.kind)
        assertEquals(expected = sourceNode.kind, actual = sourceTreeNode.kind)
    }

    @Test
    fun sourcePath_ofSourceFile_returnsPath() {
        val path = SourcePath("src/Test.java")
        val sourceTreeNode = SourceTreeNode.of(SourceFile(path))

        assertEquals(path, sourceTreeNode.sourcePath)
    }

    @Test
    fun sourcePath_ofNode_returnsPathOfContainerSourceFile() {
        val path = "src/Test.java"
        val sourceTreeNode =
            qualifiedSourcePathOf(path).build { +function("getVersion(String)") {} }

        assertEquals(path, sourceTreeNode.sourcePath.toString())
    }

    @Test
    fun castOrNull_whenValidType_returnsNode() {
        val sourceTreeNode = qualifiedSourcePathOf("src/Test.java").build {}

        assertEquals<SourceTreeNode<*>?>(
            sourceTreeNode,
            sourceTreeNode.castOrNull<SourceContainer>()
        )
    }

    @Test
    fun castOrNull_whenInvalidType_returnsNull() {
        val sourceTreeNode = qualifiedSourcePathOf("src/Test.java").build {}

        assertNull(sourceTreeNode.castOrNull<SourceEntity>())
    }

    @Test
    fun cast_whenValidType_returnsNode() {
        val sourceTreeNode = qualifiedSourcePathOf("src/Test.java").build {}

        assertEquals<SourceTreeNode<*>>(sourceTreeNode, sourceTreeNode.cast<SourceContainer>())
    }

    @Test
    fun cast_whenInvalidType_throws() {
        val sourceTreeNode = qualifiedSourcePathOf("src/Test.java").build {}

        assertFailsWith<IllegalArgumentException> { sourceTreeNode.cast<SourceEntity>() }
    }

    @Test
    fun walkSourceTree_returnsAllDescendants() {
        val sourceTreeNode = qualifiedSourcePathOf("src/Main.java").type("Main").build {
            +type("InnerType") {
                +function("main(String[])") {}
            }
            +variable("VERSION") {}
        }

        val expectedSourceTreeNodes =
            listOf(
                qualifiedSourcePathOf("src/Main.java").type("Main").build {
                    +type("InnerType") {
                        +function("main(String[])") {}
                    }
                    +variable("VERSION") {}
                },
                qualifiedSourcePathOf("src/Main.java").type("Main").type("InnerType").build {
                    +function("main(String[])") {}
                },
                qualifiedSourcePathOf("src/Main.java").type("Main").variable("VERSION").build {},
                qualifiedSourcePathOf("src/Main.java")
                    .type("Main")
                    .type("InnerType")
                    .function("main(String[])")
                    .build {}
            )

        assertEquals(expected = expectedSourceTreeNodes, actual = sourceTreeNode.walkSourceTree())
    }
}
