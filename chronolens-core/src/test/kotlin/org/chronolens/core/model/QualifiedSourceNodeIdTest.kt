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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test

class QualifiedSourceNodeIdTest {
    @Test
    fun createAbstractSourceNodeId_fails() {
        assertFailsWith<IllegalArgumentException> {
            QualifiedSourceNodeId.of<SourceEntity>(null, Identifier("Main"))
        }
    }

    @Test
    fun createSourceFileId_whenSimpleIdIsNotSourcePath_fails() {
        assertFailsWith<IllegalArgumentException> {
            QualifiedSourceNodeId.of<SourceFile>(null, Identifier("Main"))
        }
    }

    @Test
    fun createTypeId_whenSimpleIdIsNotIdentifier_fails() {
        assertFailsWith<IllegalArgumentException> {
            QualifiedSourceNodeId.of<Type>(null, SourcePath("src/Main"))
        }
    }

    @Test
    fun createFunctionId_whenSimpleIdIsNotSignature_fails() {
        assertFailsWith<IllegalArgumentException> {
            QualifiedSourceNodeId.of<Function>(null, Identifier("Main"))
        }
    }

    @Test
    fun createVariableId_whenSimpleIdIsNotIdentifier_fails() {
        assertFailsWith<IllegalArgumentException> {
            QualifiedSourceNodeId.of<Variable>(null, Signature("main()"))
        }
    }

    @Test
    fun createSourceFileId_whenNonNullParent_fails() {
        assertFailsWith<IllegalArgumentException> {
            QualifiedSourceNodeId.of<SourceFile>(
                qualifiedSourcePathOf("src/main/kotlin"),
                SourcePath("Main.java")
            )
        }
    }

    @Test
    fun createTypeId_whenNullParent_fails() {
        assertFailsWith<IllegalArgumentException> {
            QualifiedSourceNodeId.of<Type>(null, Identifier("Main"))
        }
    }

    @Test
    fun createFunctionId_whenNullParent_fails() {
        assertFailsWith<IllegalArgumentException> {
            QualifiedSourceNodeId.of<Function>(null, Signature("Main"))
        }
    }

    @Test
    fun createVariableId_whenNullParent_fails() {
        assertFailsWith<IllegalArgumentException> {
            QualifiedSourceNodeId.of<Variable>(null, Identifier("Main"))
        }
    }

    @Test
    fun sourcePath_whenNullParent_returnsId() {
        val path = SourcePath("src/main/java/Main.java")
        val qualifiedId = qualifiedSourcePathOf(path)

        assertEquals(path, qualifiedId.sourcePath)
    }

    @Test
    fun sourcePath_whenNonNullParent_returnsParentSourcePath() {
        val path = SourcePath("src/main/java/Main.java")
        val qualifiedId = qualifiedSourcePathOf(path).type("Main").function("main()")

        assertEquals(path, qualifiedId.sourcePath)
    }

    @Test
    fun castOrNull_whenValidType_returnsId() {
        val qualifiedId = qualifiedSourcePathOf("src/Main.java")

        assertEquals<QualifiedSourceNodeId<*>?>(
            qualifiedId,
            qualifiedId.castOrNull<SourceContainer>()
        )
    }

    @Test
    fun castOrNull_whenInvalidType_returnsNull() {
        val qualifiedId = qualifiedSourcePathOf("src/Main.java")

        assertNull(qualifiedId.castOrNull<SourceEntity>())
    }

    @Test
    fun cast_whenValidType_returnsId() {
        val qualifiedId = qualifiedSourcePathOf("src/Main.java")

        assertEquals<QualifiedSourceNodeId<*>>(qualifiedId, qualifiedId.cast<SourceContainer>())
    }

    @Test
    fun cast_whenInvalidType_throws() {
        val qualifiedId = qualifiedSourcePathOf("src/Main.java")

        assertFailsWith<IllegalArgumentException> { qualifiedId.cast<SourceEntity>() }
    }

    @Test
    fun isValid_whenValidId_returnsTrue() {
        val rawQualifiedIds =
            listOf(
                ".dotfile",
                "Main.java",
                "src/.dotfile",
                "src/Main.java",
                "src/Main.java:Main",
                "src/Main.java:Main#main(String[])",
                "src/Main.java:Main#VERSION",
                "src/Main.java:Main:InnerMain",
                "src/Main.java:Main:InnerMain#main(String[])",
                "src/Main.java:Main:InnerMain#VERSION",
            )

        for (rawQualifiedId in rawQualifiedIds) {
            assertTrue(
                QualifiedSourceNodeId.isValid(rawQualifiedId),
                "Id '$rawQualifiedId' should be valid!"
            )
        }
    }

    @Test
    fun isValid_whenInvalidId_returnsFalse() {
        val rawQualifiedIds =
            listOf(
                "",
                "src//Main",
                "src/./Main",
                "src/../Main",
                "/src/Main",
                "src/Main/",
                "src/Main::Main",
                "src/Main:#Main",
                "src/Main.java:Main#VERSION/file",
                "src/Main.java:Main#main()/file",
                "src/Main.java:Main:InnerMain/file",
                "src/Main.java:Main#main():VERSION",
                "src/Main.java:Main#main()#main(String[])",
            )

        for (rawQualifiedId in rawQualifiedIds) {
            assertFalse(
                QualifiedSourceNodeId.isValid(rawQualifiedId),
                "Id '$rawQualifiedId' should be invalid!"
            )
        }
    }

    @Test
    fun parseQualifiedSourceNodeIdFrom_whenInputIsQualifiedIdAsString_returnsOriginalQualifiedId() {
        val qualifiedIds =
            listOf(
                qualifiedSourcePathOf("src/Main.java"),
                qualifiedSourcePathOf("src/Main.java").type("Main"),
                qualifiedSourcePathOf("src/Main.java").function("main(String[])"),
                qualifiedSourcePathOf("src/Main.java").variable("VERSION"),
                qualifiedSourcePathOf("src/Main.java").type("Main").type("InnerMain"),
                qualifiedSourcePathOf("src/Main.java").type("Main").function("main(String[])"),
                qualifiedSourcePathOf("src/Main.java").type("Main").variable("VERSION"),
            )

        for (qualifiedId in qualifiedIds) {
            assertEquals(qualifiedId, parseQualifiedSourceNodeIdFrom(qualifiedId.toString()))
        }
    }

    @Test
    fun parseQualifiedSourceNodeIdFrom_whenInvalidId_throws() {
        val rawQualifiedIds =
            listOf(
                "",
                "src/Main::Main",
                "src/Main:#Main",
                "src/Main.java:Main#main():VERSION",
                "src/Main.java:Main#main()#main(String[])",
            )

        for (rawQualifiedId in rawQualifiedIds) {
            assertFailsWith<IllegalArgumentException>("Parsing id '$rawQualifiedId' should fail!") {
                parseQualifiedSourceNodeIdFrom(rawQualifiedId)
            }
        }
    }
}
