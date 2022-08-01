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
                QualifiedSourceNodeId.fromPath("src/main/kotlin"),
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
        val qualifiedId = QualifiedSourceNodeId.fromPath(path)

        assertEquals(path, qualifiedId.sourcePath)
    }

    @Test
    fun sourcePath_whenNonNullParent_returnsParentSourcePath() {
        val path = SourcePath("src/main/java/Main.java")
        val qualifiedId = QualifiedSourceNodeId.fromPath(path).type("Main").function("main()")

        assertEquals(path, qualifiedId.sourcePath)
    }

    @Test
    fun parseFrom_whenInputIsQualifiedIdAsString_returnsOriginalQualifiedId() {
        val qualifiedIds =
            listOf(
                QualifiedSourceNodeId.fromPath("src/Main.java"),
                QualifiedSourceNodeId.fromPath("src/Main.java").type("Main"),
                QualifiedSourceNodeId.fromPath("src/Main.java").function("main(String[])"),
                QualifiedSourceNodeId.fromPath("src/Main.java").variable("VERSION"),
                QualifiedSourceNodeId.fromPath("src/Main.java").type("Main").type("InnerMain"),
                QualifiedSourceNodeId.fromPath("src/Main.java")
                    .type("Main")
                    .function("main(String[])"),
                QualifiedSourceNodeId.fromPath("src/Main.java").type("Main").variable("VERSION"),
            )

        for (qualifiedId in qualifiedIds) {
            assertEquals(qualifiedId, QualifiedSourceNodeId.parseFrom(qualifiedId.toString()))
        }
    }

    @Test
    fun parseFrom_whenInvalidId_throws() {
        val rawQualifiedIds =
            listOf(
                "",
                "src/Main::Main",
                "src/Main:#Main",
                "src/Main.java:Main#main():VERSION",
                "src/Main.java:Main#main()#main(String[])",
            )

        for (rawQualifiedId in rawQualifiedIds) {
            assertFailsWith<IllegalArgumentException> {
                QualifiedSourceNodeId.parseFrom(rawQualifiedId)
            }
        }
    }
}
