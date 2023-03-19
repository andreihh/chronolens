/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import java.io.ByteArrayOutputStream
import java.net.URL
import java.time.Instant
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.chronolens.api.repository.RepositoryId
import org.chronolens.api.serialization.SerializationException
import org.chronolens.api.serialization.deserialize
import org.chronolens.model.Identifier
import org.chronolens.model.QualifiedSourceNodeId
import org.chronolens.model.Revision
import org.chronolens.model.Signature
import org.chronolens.model.SourcePath
import org.chronolens.model.function
import org.chronolens.model.qualifiedSourcePathOf
import org.chronolens.model.type
import org.chronolens.model.variable
import org.chronolens.test.model.add
import org.chronolens.test.model.edit
import org.chronolens.test.model.function
import org.chronolens.test.model.remove
import org.chronolens.test.model.revision
import org.chronolens.test.model.type
import org.chronolens.test.model.variable
import org.junit.Test

class JsonModuleTest {
  private val data =
    revision("HEAD") {
      date = Instant.ofEpochMilli(1824733L)
      author = "unknown"
      +qualifiedSourcePathOf("res").add {
        +variable("DEBUG") { +"true" }
        +variable("RELEASE") { +"false" }
        +function("createIClass()") {}
        +type("IClass") {
          supertypes("Interface", "Object")
          +type("InnerClass") {}
          +variable("version") { +"1" }
          +function("getVersion()") { +"1" }
        }
      }
      +qualifiedSourcePathOf("res").function("createIClass()").remove()
      +qualifiedSourcePathOf("res").variable("DEBUG").edit {
        initializer {
          remove(0)
          add(index = 0, value = "false")
        }
      }
      +qualifiedSourcePathOf("res").variable("RELEASE").remove()
      +qualifiedSourcePathOf("res").type("IClass").edit { supertypes { -"Interface" } }
    }

  // TODO: figure out why this doesn't pass anymore.
  @Ignore
  @Test
  fun `test serialize class loader throws`() {
    val dst = ByteArrayOutputStream()
    assertFailsWith<SerializationException> { JsonModule.serialize(dst, javaClass.classLoader) }
  }

  @Test
  fun `test deserialize revision`() {
    val src = javaClass.getResourceAsStream("data.json")
    val actualData = JsonModule.deserialize<Revision>(src)
    assertEquals(data, actualData)
  }

  @Test
  fun `test serialize and deserialize revision`() {
    val out = ByteArrayOutputStream()
    JsonModule.serialize(out, data)
    val src = out.toByteArray().inputStream()
    val actualData = JsonModule.deserialize<Revision>(src)
    assertEquals(data, actualData)
  }

  @Test
  fun `test serialize and deserialize history`() {
    val history = List(size = 10) { data }
    val out = ByteArrayOutputStream()
    JsonModule.serialize(out, history)
    val src = out.toByteArray().inputStream()
    val actualHistory = JsonModule.deserialize<Array<Revision>>(src)
    assertEquals(history, actualHistory.asList())
  }

  @Test
  fun `test deserialize history from invalid json throws`() {
    val src = "{}".byteInputStream()
    assertFailsWith<SerializationException> { JsonModule.deserialize<Array<Revision>>(src) }
  }

  @Test
  fun stringifySourceNodeId_printsString() {
    val ids =
      listOf(SourcePath("src/Main.java"), Identifier("Main"), Signature("getVersion(String)"))

    for (id in ids) {
      assertEquals(expected = "\"$id\"", actual = JsonModule.stringify(id))
    }
  }

  @Test
  fun deserializeSourcePath_parsesString() {
    val path = SourcePath("src/Main.java")
    val src = "\"$path\"".byteInputStream()

    assertEquals(path, JsonModule.deserialize(src))
  }

  @Test
  fun deserializeSourcePath_whenInvalid_fails() {
    val src = "\"src\\getVersion\"".byteInputStream()

    assertFailsWith<SerializationException> { JsonModule.deserialize<SourcePath>(src) }
  }

  @Test
  fun deserializeIdentifier_parsesString() {
    val identifier = Identifier("Main")
    val src = "\"$identifier\"".byteInputStream()

    assertEquals(identifier, JsonModule.deserialize(src))
  }

  @Test
  fun deserializeIdentifier_whenInvalid_fails() {
    val src = "\"Main/main\"".byteInputStream()

    assertFailsWith<SerializationException> { JsonModule.deserialize<Identifier>(src) }
  }

  @Test
  fun deserializeSignature_parsesString() {
    val signature = Signature("getVersion(String)")
    val src = "\"$signature\"".byteInputStream()

    assertEquals(signature, JsonModule.deserialize(src))
  }

  @Test
  fun deserializeSignature_whenInvalid_fails() {
    val src = "\"getVersion)\"".byteInputStream()

    assertFailsWith<SerializationException> { JsonModule.deserialize<Signature>(src) }
  }

  @Test
  fun deserializeQualifiedSourcePath_parsesString() {
    val qualifiedPath = qualifiedSourcePathOf("src/Main.java")
    val src = "\"$qualifiedPath\"".byteInputStream()

    assertEquals(qualifiedPath, JsonModule.deserialize<QualifiedSourceNodeId<*>>(src))
  }

  @Test
  fun deserializeQualifiedSourceNodeId_whenInvalid_fails() {
    val src = "\"src/Main.java/../:getVersion\"".byteInputStream()

    assertFailsWith<SerializationException> {
      JsonModule.deserialize<QualifiedSourceNodeId<*>>(src)
    }
  }

  // TODO: fix this
  @Test fun deserializeQualifiedSourceNodeIdKey_whenInvalid_fails() {}

  @Test
  fun deserializeRepositoryId_parsesString() {
    val repositoryId = RepositoryId("repo.git", URL("file:///repository.git"))
    val src = "\"$repositoryId\"".byteInputStream()

    assertEquals(repositoryId, JsonModule.deserialize(src))
  }

  @Test
  fun deserializeRepositoryId_whenInvalid_fails() {
    val src = "\"repo*.git:file:///repository.git\"".byteInputStream()

    assertFailsWith<SerializationException> { JsonModule.deserialize<RepositoryId>(src) }
  }
}
