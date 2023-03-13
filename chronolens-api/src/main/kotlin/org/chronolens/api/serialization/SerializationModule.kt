/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.api.serialization

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import org.chronolens.api.analysis.Report
import org.chronolens.api.repository.RepositoryId
import org.chronolens.model.ListEdit
import org.chronolens.model.QualifiedSourceNodeId
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId
import org.chronolens.model.SetEdit
import org.chronolens.model.SourceNode
import org.chronolens.model.SourceNodeId
import org.chronolens.model.SourceTree
import org.chronolens.model.SourceTreeEdit
import org.chronolens.model.SourceTreeNode

/**
 * Provides serialization and deserialization of arbitrary objects.
 *
 * Must be able to serialize and deserialize:
 * - [SourceNodeId]s (sealed polymorphic type) as [String]s
 * - [SourceNode]s (sealed polymorphic type)
 * - [QualifiedSourceNodeId]s as [String]s
 * - [SourceTreeNode]s
 * - [SourceTree]s
 * - [ListEdit]s (sealed polymorphic type)
 * - [SetEdit]s (sealed polymorphic type)
 * - [SourceTreeEdit]s (sealed polymorphic type)
 * - [RevisionId]s as [String]s
 * - [Revision]s
 * - [RepositoryId]s as [String]s
 * - [Report]s (open polymorphic type)
 */
public interface SerializationModule {
  /**
   * Serializes the given [value] object to the given [out] stream.
   *
   * @throws SerializationException if there are any serialization errors
   * @throws IOException if there are any output related errors
   */
  @Throws(IOException::class) public fun serialize(out: OutputStream, value: Any)

  /**
   * Serializes the given [value] object into a [String].
   *
   * @throws SerializationException if there are any serialization errors
   * @throws IOException if there are any output related errors
   */
  @Throws(IOException::class)
  public fun stringify(value: Any): String =
    ByteArrayOutputStream().use { out ->
      serialize(out, value)
      out.toByteArray().decodeToString()
    }

  /**
   * Deserializes an object of the given non-generic [type] from the given [src] stream.
   *
   * @throws SerializationException if there are any deserialization errors
   * @throws IOException if there are any input related errors
   */
  @Throws(IOException::class) public fun <T : Any> deserialize(src: InputStream, type: Class<T>): T
}

/** Inline utility method. */
@Throws(IOException::class)
public inline fun <reified T : Any> SerializationModule.deserialize(src: InputStream): T =
  deserialize(src, T::class.java)
