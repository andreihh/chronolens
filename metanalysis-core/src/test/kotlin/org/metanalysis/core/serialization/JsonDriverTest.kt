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

package org.metanalysis.core.serialization

import org.junit.Test

import org.metanalysis.core.Node.Function
import org.metanalysis.core.Node.Type
import org.metanalysis.core.Node.Variable
import org.metanalysis.core.delta.FunctionTransaction
import org.metanalysis.core.delta.ListEdit
import org.metanalysis.core.delta.NodeSetEdit
import org.metanalysis.core.delta.SetEdit
import org.metanalysis.core.delta.SourceFileTransaction
import org.metanalysis.core.delta.TypeTransaction
import org.metanalysis.core.delta.VariableTransaction

import java.io.ByteArrayOutputStream

import kotlin.test.assertEquals

class JsonDriverTest {
    private val data = SourceFileTransaction(listOf(
            NodeSetEdit.Add(Type(
                    name = "IClass",
                    supertypes = setOf("Interface", "Object"),
                    members = setOf(
                            Type("InnerClass"),
                            Variable("version", "1"),
                            Function("getVersion()", emptyList(), "{return 1;}")
                    )
            )),
            NodeSetEdit.Remove<Function>("createIClass()"),
            NodeSetEdit.Change<Variable>(
                    identifier = "DEBUG",
                    transaction = VariableTransaction(listOf(
                            ListEdit.Add(0, "true")
                    ))
            ),
            NodeSetEdit.Remove<Variable>("RELEASE"),
            NodeSetEdit.Change<Type>("Interface", TypeTransaction(
                    supertypeEdits = listOf(SetEdit.Remove("Object")),
                    memberEdits = listOf(NodeSetEdit.Change<Function>(
                            "getVersion()",
                            FunctionTransaction(
                                    parameterEdits = listOf(ListEdit.Remove(0)),
                                    bodyEdits = listOf(ListEdit.Remove(0))
                            )
                    ))
            ))
    ))

    @Test fun `test serialize source file transaction`() {
        val bos = ByteArrayOutputStream()
        JsonDriver.serialize(bos, data)
        println(bos.toString())
    }

    @Test fun `test deserialize source file transaction`() {
        val bos = ByteArrayOutputStream()
        JsonDriver.serialize(bos, data)
        val actualData = JsonDriver.deserialize<SourceFileTransaction>(
                src = bos.toByteArray().inputStream()
        )
        assertEquals(data, actualData)
        println(actualData)
    }
}
