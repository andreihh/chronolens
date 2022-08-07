/*
 * Copyright 2017-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import kotlin.test.assertFailsWith
import org.junit.Test

class ValidationUtilsTest {
    @Test
    fun `test empty source file id throws`() {
        assertFailsWith<IllegalArgumentException> { validateFileId("") }
    }

    @Test
    fun `test entity separator in source file id throws`() {
        assertFailsWith<IllegalArgumentException> { validateFileId("src:Test.java") }
    }

    @Test
    fun `test source file id without separators is valid`() {
        validateFileId("Test.java")
    }

    @Test
    fun `test source file id with correct path separator is valid`() {
        validateFileId("src/Test.java")
    }

    @Test
    fun `test type id with correct separator is valid`() {
        validateTypeId("src/Test.java:IClass")
    }

    @Test
    fun `test nested type id with correct separators is valid`() {
        validateTypeId("src/Test.java:IClass:InnerClass")
    }

    @Test
    fun `test nested type id with path separator throws`() {
        assertFailsWith<IllegalArgumentException> {
            validateTypeId("src/Test.java:IClass/InnerClass")
        }
    }

    @Test
    fun `test function id with path separator throws`() {
        assertFailsWith<IllegalArgumentException> {
            validateFunctionId("Test.java:IClass/getVersion()")
        }
    }

    @Test
    fun `test function id with invalid parentheses throws`() {
        assertFailsWith<IllegalArgumentException> {
            validateFunctionId("Test.java#getVersion(String")
        }
    }

    @Test
    fun `test function id with invalid parameter type throws`() {
        assertFailsWith<IllegalArgumentException> {
            validateFunctionId("Test.java#getVersion(Str:ing)")
        }
    }

    @Test
    fun `test function id with no parameters is valid`() {
        validateFunctionId("Test.java#getVersion()")
    }

    @Test
    fun `test function id with single parameter is valid`() {
        validateFunctionId("Test.java#getVersion(String)")
    }

    @Test
    fun `test function id with multiple parameters is valid`() {
        validateFunctionId("Test.java#getVersion(int, String, int)")
    }

    @Test
    fun `test function id with vararg parameter type is valid`() {
        validateFunctionId("Test.java#getVersion(String...)")
    }

    @Test
    fun `test function id with lambda parameter type is valid`() {
        validateFunctionId("Test.java#getVersion((T) -> Unit)")
    }

    @Test
    fun `test variable id with parentheses throws`() {
        assertFailsWith<IllegalArgumentException> { validateVariableId("Test.java:version()") }
    }

    @Test
    fun `test valid function id is valid entity id`() {
        validateEntityId("src/Test.java:IClass#getVersion()")
    }

    @Test
    fun `test valid source file id is invalid entity id`() {
        assertFailsWith<IllegalArgumentException> { validateEntityId("src/Test.java") }
    }
}
