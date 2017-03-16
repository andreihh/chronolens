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

package org.chronos.core.delta

import org.chronos.core.Node.Function
import org.chronos.core.Node.Type
import org.chronos.core.Node.Variable
import org.chronos.core.SourceFile
import org.chronos.core.delta.SourceFileTransaction.Companion.diff
import org.chronos.core.delta.Transaction.Companion.apply
import org.chronos.java.JavaParser
import org.chronos.test.assertEquals

import org.junit.Test

import java.net.URL

class SourceFileTransactionTest {
    private fun assertDiff(src: SourceFile, dst: SourceFile) {
        assertEquals(src.apply(src.diff(dst)), dst)
        assertEquals(dst.apply(dst.diff(src)), src)
    }

    @Test fun `test add type`() {
        val addedType = Type("IInterface")
        val expected = SourceFile(setOf(addedType))
        val actual = SourceFile(emptySet()).apply(SourceFileTransaction(
                nodeEdits = listOf(NodeSetEdit.Add(addedType))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test diff equal empty source files`() {
        val src = SourceFile(emptySet())
        val dst = SourceFile(emptySet())
        assertDiff(src, dst)
    }

    @Test fun `test diff empty source to single type`() {
        val src = SourceFile(emptySet())
        val dst = SourceFile(setOf(Type("IClass")))
        assertDiff(src, dst)
    }

    @Test fun `test diff integration`() {
        val src = SourceFile(setOf(
                Type(
                        name = "IClass",
                        members = setOf(
                                Variable("version"),
                                Variable("name"),
                                Function("getVersion()", emptyList()),
                                Type("IInterface")
                        )
                ),
                Variable("DEBUG_LEVEL")
        ))
        val dst = SourceFile(setOf(
                Type(
                        name = "IClass",
                        supertypes = setOf("Comparable<IClass>"),
                        members = setOf(
                                Variable("version", "1"),
                                Function(
                                        signature = "getVersion()",
                                        parameters = emptyList(),
                                        body = "{\n  return version;\n}\n"
                                ),
                                Type(
                                        name = "IInterface",
                                        supertypes = setOf("IClass"),
                                        members = setOf(Variable("name"))
                                ),
                                Function(
                                        signature = "compare(IClass)",
                                        parameters = listOf(Variable("other")),
                                        body = "{return version-other.version;}"
                                )
                        )
                ),
                Variable("DEBUG", "true"),
                Function("main(String[])", listOf(Variable("args")), "{}")
        ))
        assertDiff(src, dst)
    }

    @Test fun `test diff network`() {
        val srcUrl = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/826e565b7cfba8de05f9f652c1541df8e8e7efe2/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
        val dstUrl = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/5e946c270018c71bf25778bc2dc25e5a9dd809b0/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
        val parser = JavaParser()
        val src = parser.parse(srcUrl)
        val dst = parser.parse(dstUrl)
        assertDiff(src, dst)
    }
}
