/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.metanalysis.core.delta

import org.junit.Test

import org.metanalysis.core.delta.SourceFileTransaction.Companion.diff
import org.metanalysis.core.model.Parser

import java.io.File

class DeltaPrettyPrinterTest {
    @Test fun `test integration visit source file transaction`() {
        // the original file can be found at: https://raw.githubusercontent.com/spring-projects/spring-framework/826e565b7cfba8de05f9f652c1541df8e8e7efe2/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java
        val srcFile = File("src/test/resources/GenericTypeResolver-v1.mock")
        // the original file can be found at: https://raw.githubusercontent.com/spring-projects/spring-framework/5e946c270018c71bf25778bc2dc25e5a9dd809b0/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java
        val dstFile = File("src/test/resources/GenericTypeResolver-v2.mock")
        val src = requireNotNull(Parser.parse(srcFile))
        val dst = requireNotNull(Parser.parse(dstFile))
        val transaction = requireNotNull(src.diff(dst))
        DeltaPrettyPrinter(System.out).visit(transaction)
    }
}
