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

package org.chronos.core.serialization

import org.chronos.core.Node.Function
import org.chronos.core.Node.Type
import org.chronos.core.Node.Variable
import org.chronos.core.delta.FunctionTransaction
import org.chronos.core.delta.ListEdit
import org.chronos.core.delta.NodeSetEdit
import org.chronos.core.delta.SetEdit
import org.chronos.core.delta.SourceFileTransaction
import org.chronos.core.delta.TypeTransaction
import org.chronos.core.delta.VariableTransaction

import org.junit.Test

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
            NodeSetEdit.Change<Variable>("DEBUG", VariableTransaction("true")),
            NodeSetEdit.Remove<Variable>("RELEASE"),
            NodeSetEdit.Change<Type>("Interface", TypeTransaction(
                    supertypeEdits = listOf(SetEdit.Remove("Object")),
                    memberEdits = listOf(NodeSetEdit.Change<Function>(
                            "getVersion()",
                            FunctionTransaction(
                                    parameterEdits = listOf(ListEdit.Remove(0)),
                                    bodyEdit = null
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
