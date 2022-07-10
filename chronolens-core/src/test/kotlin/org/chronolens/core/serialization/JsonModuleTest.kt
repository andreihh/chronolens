/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.serialization

import org.chronolens.core.model.Identifier
import org.chronolens.core.model.Signature
import org.chronolens.core.model.SourcePath
import org.chronolens.core.repository.Transaction
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.repository.transaction
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.Instant
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonModuleTest {
    private val data = transaction("HEAD") {
        date = Instant.ofEpochMilli(1824733L)
        author = "unknown"
        +sourceFile("res").add {
            variable("DEBUG") { +"true" }
            variable("RELEASE") { +"false" }
            function("createIClass()") {}
            type("IClass") {
                supertypes("Interface", "Object")
                type("InnerClass") {}
                variable("version") { +"1" }
                function("getVersion()") { +"1" }
            }
        }
        +sourceFile("res").function("createIClass()").remove()
        +sourceFile("res").variable("DEBUG").edit {
            initializer {
                remove(0)
                add(index = 0, value = "false")
            }
        }
        +sourceFile("res").variable("RELEASE").remove()
        +sourceFile("res").type("IClass").edit {
            supertypes { -"Interface" }
        }
    }

    // TODO: figure out why this doesn't pass anymore.
    @Ignore
    @Test
    fun `test serialize class loader throws`() {
        val dst = ByteArrayOutputStream()
        assertFailsWith<JsonException> {
            JsonModule.serialize(dst, javaClass.classLoader)
        }
    }

    @Test fun `test deserialize transaction`() {
        val src = javaClass.getResourceAsStream("data.json")
        val actualData = JsonModule.deserialize<Transaction>(src)
        assertEquals(data, actualData)
    }

    @Test fun `test serialize and deserialize transaction`() {
        val out = ByteArrayOutputStream()
        JsonModule.serialize(out, data)
        val src = out.toByteArray().inputStream()
        val actualData = JsonModule.deserialize<Transaction>(src)
        assertEquals(data, actualData)
    }

    @Test fun `test serialize and deserialize history`() {
        val history = List(size = 10) { data }
        val out = ByteArrayOutputStream()
        JsonModule.serialize(out, history)
        val src = out.toByteArray().inputStream()
        val actualHistory = JsonModule.deserialize<Array<Transaction>>(src)
        assertEquals(history, actualHistory.asList())
    }

    @Test fun `test deserialize history from invalid json throws`() {
        val src = "{}".byteInputStream()
        assertFailsWith<JsonException> {
            JsonModule.deserialize<Array<Transaction>>(src)
        }
    }

    @Test fun serializeSourceNodeId_printsString() {
        val ids = listOf(
            SourcePath("src/Main.java"),
            Identifier("Main"),
            Signature("getVersion(String)")
        )

        for (id in ids) {
            val out = ByteArrayOutputStream()
            JsonModule.serialize(out, id)

            assertEquals("\"$id\"", out.toByteArray().decodeToString())
        }
    }

    @Test fun deserializeSourcePath_parsesString() {
        val path = SourcePath("src/Main.java")
        val src = "\"$path\"".byteInputStream()

        assertEquals(path, JsonModule.deserialize(src))
    }

    @Test fun deserializeSourcePath_whenInvalid_fails() {
        val src = "\"src\\getVersion\"".byteInputStream()

        assertFailsWith<JsonException> {
            JsonModule.deserialize<SourcePath>(src)
        }
    }

    @Test fun deserializeIdentifier_parsesString() {
        val identifier = Identifier("Main")
        val src = "\"$identifier\"".byteInputStream()

        assertEquals(identifier, JsonModule.deserialize(src))
    }

    @Test fun deserializeIdentifier_whenInvalid_fails() {
        val src = "\"Main/main\"".byteInputStream()

        assertFailsWith<JsonException> {
            JsonModule.deserialize<Identifier>(src)
        }
    }

    @Test fun deserializeSignature_parsesString() {
        val signature = Signature("getVersion(String)")
        val src = "\"$signature\"".byteInputStream()

        assertEquals(signature, JsonModule.deserialize(src))
    }

    @Test fun deserializeSignature_whenInvalid_fails() {
        val src = "\"getVersion)\"".byteInputStream()

        assertFailsWith<JsonException> {
            JsonModule.deserialize<Signature>(src)
        }
    }
}
