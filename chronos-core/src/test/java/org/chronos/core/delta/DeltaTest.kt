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
import org.chronos.core.delta.Transaction.Companion.apply
import org.chronos.test.assertEquals

import org.junit.Test

class DeltaTest {
    fun test(src: SourceFile, dst: SourceFile) {
        assertEquals(dst, src.apply(src.diff(dst)))
    }

    fun test(src: Type, dst: Type) {
        assertEquals(dst, src.apply(src.diff(dst)))
    }

    fun test(src: Variable, dst: Variable) {
        assertEquals(dst, src.apply(src.diff(dst)))
    }

    fun test(src: Function, dst: Function) {
        assertEquals(dst, src.apply(src.diff(dst)))
    }

    @Test fun `test equal empty source files`() {
        val src = SourceFile(emptySet())
        val dst = SourceFile(emptySet())
        test(src, dst)
    }

    @Test fun `test empty source to single type`() {
        val src = SourceFile(emptySet())
        val dst = SourceFile(setOf(Type("IClass")))
        test(src, dst)
    }

    @Test fun `test integration`() {
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
        test(src, dst)
    }
}
