/*
 * Copyright 2021-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import java.lang.IllegalArgumentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.chronolens.test.core.model.sourceFile

class QualifiedIdTest {
    @Test
    fun sourcePath_whenNoParent_returnsId() {
        val path = "src/main/java/Main.java"
        val qualifier = qualifiedPathOf(path)

        assertEquals(SourcePath(path), qualifier.sourcePath)
    }

    @Test
    fun sourcePath_whenParentIsSourceFile_returnsParentId() {
        val path = "src/main/java/Main.java"
        val type = "Main"
        val qualifier = qualifiedPathOf(path).appendType(type)

        assertEquals(SourcePath(path), qualifier.sourcePath)
    }

    @Test
    fun parseQualifiedIdFromString_returnsOriginalQualifiedId() {
        val qualifiedIds =
            listOf(
                qualifiedPathOf("src/Main.java"),
                qualifiedPathOf("src/Main.java").appendType("Main"),
                qualifiedPathOf("src/Main.java").appendFunction("main(String[])"),
                qualifiedPathOf("src/Main.java").appendVariable("VERSION"),
                qualifiedPathOf("src/Main.java").appendType("Main").appendType("InnerMain"),
                qualifiedPathOf("src/Main.java")
                    .appendType("Main")
                    .appendFunction("main(String[])"),
                qualifiedPathOf("src/Main.java").appendType("Main").appendVariable("VERSION"),
            )

        for (qualifiedId in qualifiedIds) {
            assertEquals(
                expected = qualifiedId,
                actual = parseQualifiedIdFrom(qualifiedId.toString()),
            )
        }
    }

    @Test
    fun parseQualifiedIdFromString_whenInvalidId_throws() {
        val rawQualifiedIds =
            listOf(
                "",
                "src/Main::Main",
                "src/Main:#Main",
                "src/Main.java:Main#main():VERSION",
                "src/Main.java:Main#main()#main(String[])",
            )

        for (rawQualifiedId in rawQualifiedIds) {
            assertFailsWith<IllegalArgumentException> { parseQualifiedIdFrom(rawQualifiedId) }
        }
    }

    @Test
    fun sourcePath_ofSourceFile_returnsPath() {
        val path = "src/Test.java"
        val source = SourceFile(path)
        val sourceTreeNode = SourceTreeNode(path, source)

        assertEquals(path, sourceTreeNode.sourcePath)
    }

    @Test
    fun sourcePath_ofNode_returnsPathOfContainerSourceFile() {
        val qualifiedId = "src/Test.java#getVersion(String)"
        val path = "src/Test.java"
        val node = sourceFile(path).function("getVersion(String)").build {}
        val sourceTreeNode = SourceTreeNode(qualifiedId, node)

        assertEquals(path, sourceTreeNode.sourcePath)
    }
}
