/*
 * Copyright 2018-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.test.core.model.sourceFile
import org.junit.Test
import kotlin.test.assertEquals

class UtilsTest {
    @Test fun `test source path of source file`() {
        val source = sourceFile("src/Test.java").build {}
        assertEquals(source.path, source.sourcePath)
    }

    @Test fun `test source path of node`() {
        val path = "src/Test.java"
        val node = sourceFile(path).function("getVersion(String)").build {}
        assertEquals(path, node.sourcePath)
    }
}
