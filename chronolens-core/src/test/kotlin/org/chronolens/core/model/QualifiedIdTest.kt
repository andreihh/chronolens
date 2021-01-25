/*
 * Copyright 2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import kotlin.test.Test
import kotlin.test.assertEquals

class QualifiedIdTest {
    @Test fun sourcePath_whenNoParent_returnsId() {
        val path = "src/main/java/Main.java"
        val qualifier = qualifiedIdOf(path)

        assertEquals(path, qualifier.sourcePath)
    }

    @Test fun sourcePath_whenParentIsSourceFile_returnsParentId() {
        val path = "src/main/java/Main.java"
        val type = "Main"
        val qualifier = qualifiedIdOf(path, type)

        assertEquals(path, qualifier.sourcePath)
    }
}
