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

import org.metanalysis.core.delta.FunctionTransaction
import org.metanalysis.core.delta.ListEdit
import org.metanalysis.core.delta.NodeSetEdit
import org.metanalysis.core.delta.SetEdit
import org.metanalysis.core.delta.SourceFileTransaction
import org.metanalysis.core.delta.TypeTransaction
import org.metanalysis.core.delta.VariableTransaction
import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.core.project.Project.HistoryEntry

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Date

import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonDriverTest {
    private val data = SourceFileTransaction(listOf(
            NodeSetEdit.Add(Type(
                    name = "IClass",
                    supertypes = setOf("Interface", "Object"),
                    members = setOf(
                            Type("InnerClass"),
                            Variable("version", emptySet(), listOf("1")),
                            Function(
                                    signature = "getVersion()",
                                    parameters = emptyList(),
                                    body = listOf("{return 1;}")
                            )
                    )
            )),
            NodeSetEdit.Remove<Function>("createIClass()"),
            NodeSetEdit.Change<Variable>(
                    identifier = "DEBUG",
                    transaction = VariableTransaction(initializerEdits = listOf(
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

    @Test fun `test deserialize invalid KClass throws`() {
        val src = "\"java.lang.String$\"".byteInputStream()
        assertFailsWith<IOException> {
            JsonDriver.deserialize<KClass<*>>(src)
        }
    }

    @Test fun `test serialize and deserialize source file transaction`() {
        val out = ByteArrayOutputStream()
        JsonDriver.serialize(out, data)
        val src = out.toByteArray().inputStream()
        val actualData = JsonDriver.deserialize<SourceFileTransaction>(src)
        assertEquals(data, actualData)
    }

    @Test fun `test serialize and deserialize history`() {
        val history = (1 until 10).map { i ->
            HistoryEntry("$i", Date(i.toLong()), "<author>", null)
        }
        val out = ByteArrayOutputStream()
        JsonDriver.serialize(out, history)
        val src = out.toByteArray().inputStream()
        val actualHistory = JsonDriver.deserialize<Array<HistoryEntry>>(src)
                .asList()
        assertEquals(history, actualHistory)
    }

    /*@Test fun gen() {
        val url1 = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/826e565b7cfba8de05f9f652c1541df8e8e7efe2/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
        val url2 = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/5e946c270018c71bf25778bc2dc25e5a9dd809b0/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
    }*/
}
