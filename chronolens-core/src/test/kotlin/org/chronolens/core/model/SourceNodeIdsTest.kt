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

import kotlin.test.Test
import kotlin.test.assertFailsWith

class SourceNodeIdsTest {
    @Test
    fun emptySourcePath_isInvalid() {
        assertFailsWith<IllegalArgumentException> { SourcePath("") }
    }

    @Test
    fun backslashInSourcePath_isInvalid() {
        assertFailsWith<IllegalArgumentException> { SourcePath("src\\Main.java") }
    }

    @Test
    fun relativeToCurrentDirectoryInSourcePath_isInvalid() {
        assertFailsWith<IllegalArgumentException> { SourcePath("src/./Main.mock") }
    }

    @Test
    fun relativeToParentDirectoryInSourcePath_isInvalid() {
        assertFailsWith<IllegalArgumentException> { SourcePath("src/../Main.mock") }
    }

    @Test
    fun doubleSlashInSourcePath_isInvalid() {
        assertFailsWith<IllegalArgumentException> { SourcePath("src//Main.mock") }
    }

    @Test
    fun slashAtBeginningInSourcePath_isInvalid() {
        assertFailsWith<IllegalArgumentException> { SourcePath("/src/Main.mock") }
    }

    @Test
    fun slashAtEndInSourcePath_isInvalid() {
        assertFailsWith<IllegalArgumentException> { SourcePath("src/Main.mock/") }
    }

    @Test
    fun containerSeparatorInSourcePath_isInvalid() {
        assertFailsWith<IllegalArgumentException> { SourcePath("src/Main.mock:Main") }
    }

    @Test
    fun memberSeparatorInSourcePath_isInvalid() {
        assertFailsWith<IllegalArgumentException> { SourcePath("src/Main.mock#Main") }
    }

    @Test
    fun validSourcePath_isCreatedSuccessfully() {
        SourcePath("src/Main.java")
    }

    @Test
    fun emptyIdentifier_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Identifier("") }
    }

    @Test
    fun slashInIdentifier_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Identifier("Main/version") }
    }

    @Test
    fun backslashInIdentifier_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Identifier("Main\\version") }
    }

    @Test
    fun parensInIdentifier_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Identifier("version(") }
        assertFailsWith<IllegalArgumentException> { Identifier("version)") }
        assertFailsWith<IllegalArgumentException> { Identifier("version()") }
    }

    @Test
    fun containerSeparatorInIdentifier_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Identifier("Main:InnerMain") }
    }

    @Test
    fun memberSeparatorInIdentifier_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Identifier("Main#version") }
    }

    @Test
    fun validIdentifier_isCreatedSuccessfully() {
        Identifier("Main")
    }

    @Test
    fun emptySignature_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Signature("") }
    }

    @Test
    fun slashInSignature_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Signature("Main/getVersion()") }
    }

    @Test
    fun backslashInSignature_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Signature("Main\\getVersion()") }
    }

    @Test
    fun missingParensInSignature_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Signature("getVersion") }
        assertFailsWith<IllegalArgumentException> { Signature("getVersion(") }
        assertFailsWith<IllegalArgumentException> { Signature("getVersion)") }
    }

    @Test
    fun containerSeparatorInSignature_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Signature("get:version()") }
    }

    @Test
    fun memberSeparatorInSignature_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Signature("get#version") }
    }

    @Test
    fun slashInSignatureParameter_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Signature("getVersion(Str/ing)") }
    }

    @Test
    fun backslashInSignatureParameter_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Signature("getVersion(Str\\ing)") }
    }

    @Test
    fun containerSeparatorInSignatureParameter_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Signature("getVersion(Str:ing)") }
    }

    @Test
    fun memberSeparatorInSignatureParameter_isInvalid() {
        assertFailsWith<IllegalArgumentException> { Signature("getVersion(Str:ing)") }
    }

    @Test
    fun validSignature_isCreatedSuccessfully() {
        Signature("getMinorVer(String app, Set<T> majorVer)")
    }

    @Test
    fun validSignatureWithLambdaParameter_isCreatedSuccessfully() {
        Signature("getVersion((T) -> Unit)")
    }

    @Test
    fun validSignatureWithVarargParameter_isCreatedSuccessfully() {
        Signature("getVersion(String...)")
    }
}
