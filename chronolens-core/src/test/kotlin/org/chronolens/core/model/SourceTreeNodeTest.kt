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
import org.junit.Test

class SourceTreeNodeTest {
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
            QualifiedSourceNodeId.fromPath(path).build { +function("getVersion(String)") {} }

        assertEquals(path, sourceTreeNode.sourcePath.toString())
    }

    @Test
    fun castOrNull_whenValidType_returnsNode() {
        val sourceTreeNode = QualifiedSourceNodeId.fromPath("src/Test.java").build {}

        assertEquals<SourceTreeNode<*>?>(
            sourceTreeNode,
            sourceTreeNode.castOrNull<SourceContainer>()
        )
    }

    @Test
    fun castOrNull_whenInvalidType_returnsNull() {
        val sourceTreeNode = QualifiedSourceNodeId.fromPath("src/Test.java").build {}

        assertNull(sourceTreeNode.castOrNull<SourceEntity>())
    }

    @Test
    fun cast_whenValidType_returnsNode() {
        val sourceTreeNode = QualifiedSourceNodeId.fromPath("src/Test.java").build {}

        assertEquals<SourceTreeNode<*>>(sourceTreeNode, sourceTreeNode.cast<SourceContainer>())
    }

    @Test
    fun cast_whenInvalidType_throws() {
        val sourceTreeNode = QualifiedSourceNodeId.fromPath("src/Test.java").build {}

        assertFailsWith<IllegalArgumentException> { sourceTreeNode.cast<SourceEntity>() }
    }
}
