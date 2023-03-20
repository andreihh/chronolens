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

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import org.chronolens.api.repository.RepositoryId
import org.chronolens.api.serialization.SerializationException
import org.chronolens.api.serialization.SerializationModule
import org.chronolens.model.AddNode
import org.chronolens.model.EditFunction
import org.chronolens.model.EditType
import org.chronolens.model.EditVariable
import org.chronolens.model.Function
import org.chronolens.model.Identifier
import org.chronolens.model.ListEdit
import org.chronolens.model.QualifiedSourceNodeId
import org.chronolens.model.RemoveNode
import org.chronolens.model.RevisionId
import org.chronolens.model.SetEdit
import org.chronolens.model.Signature
import org.chronolens.model.SourceFile
import org.chronolens.model.SourceNode
import org.chronolens.model.SourceNodeId
import org.chronolens.model.SourcePath
import org.chronolens.model.SourceTreeEdit
import org.chronolens.model.Type
import org.chronolens.model.Variable

/** Provides JSON serialization and deserialization of arbitrary objects. */
public object JsonModule : SerializationModule {
  private val typeIdResolver =
    object : TypeIdResolverBase() {
        private val typeToId =
          mapOf(
            SourceFile::class.java to "SourceFile",
            Type::class.java to "Type",
            Function::class.java to "Function",
            Variable::class.java to "Variable",
            ListEdit.Add::class.java to "List.Add",
            ListEdit.Remove::class.java to "List.Remove",
            SetEdit.Add::class.java to "Set.Add",
            SetEdit.Remove::class.java to "Set.Remove",
            AddNode::class.java to "AddNode",
            RemoveNode::class.java to "RemoveNode",
            EditType::class.java to "EditType",
            EditFunction::class.java to "EditFunction",
            EditVariable::class.java to "EditVariable"
          )

        private val idToType = typeToId.map { (type, id) -> id to type }.toMap()

        override fun idFromValueAndType(value: Any?, suggestedType: Class<*>): String =
          typeToId.getValue(suggestedType)

        override fun idFromValue(value: Any): String = typeToId.getValue(value.javaClass)

        override fun typeFromId(context: DatabindContext, id: String): JavaType =
          context.typeFactory.constructType(idToType.getValue(id))

        override fun getMechanism(): JsonTypeInfo.Id = JsonTypeInfo.Id.NAME
      }
      .apply { init(null) }

  private val typeResolver =
    object : DefaultTypeResolverBuilder(OBJECT_AND_NON_CONCRETE) {
        private val abstractTypes =
          listOf(
            SourceNode::class.java,
            SourceTreeEdit::class.java,
            ListEdit::class.java,
            SetEdit::class.java,
          )

        override fun useForType(t: JavaType): Boolean =
          abstractTypes.any { it.isAssignableFrom(t.rawClass) }
      }
      .init(JsonTypeInfo.Id.NAME, typeIdResolver)
      .inclusion(JsonTypeInfo.As.PROPERTY)
      .typeProperty("@class")

  private inline fun <reified T> SimpleModule.addDeserializer(deserializer: StdDeserializer<T>) =
    addDeserializer(T::class.java, deserializer)

  private inline fun <reified T> SimpleModule.addKeySerializer(serializer: StdSerializer<T>) =
    addKeySerializer(T::class.java, serializer)

  private inline fun <reified T> SimpleModule.addKeyDeserializer(deserializer: KeyDeserializer) =
    addKeyDeserializer(T::class.java, deserializer)

  private val qualifiedIdModule =
    SimpleModule()
      .addSerializer(SourceNodeIdSerializer)
      .addDeserializer(SourcePathDeserializer)
      .addDeserializer(IdentifierDeserializer)
      .addDeserializer(SignatureDeserializer)
      .addSerializer(RevisionIdSerializer)
      .addDeserializer(RevisionIdDeserializer)
      .addSerializer(QualifiedSourceNodeIdSerializer)
      .addDeserializer(QualifiedSourceNodeIdDeserializer)
      .addKeySerializer(QualifiedSourceNodeIdSerializer)
      .addKeyDeserializer<QualifiedSourceNodeId<*>>(QualifiedSourceNodeIdKeyDeserializer)
      .addSerializer(RepositoryIdSerializer)
      .addDeserializer(RepositoryIdDeserializer)

  private val objectMapper =
    jacksonMapperBuilder()
      .apply {
        addModules(JavaTimeModule(), qualifiedIdModule)
        setDefaultTyping(typeResolver)
        disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        enable(SerializationFeature.INDENT_OUTPUT)
      }
      .build()
      .apply {
        setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
      }

  @Throws(IOException::class)
  public override fun serialize(out: OutputStream, value: Any) {
    try {
      objectMapper.writeValue(out, value)
    } catch (e: JsonProcessingException) {
      throw SerializationException(e)
    }
  }

  @Throws(IOException::class)
  public override fun <T : Any> deserialize(src: InputStream, type: Class<T>): T =
    try {
      objectMapper.readValue(src, type)
    } catch (e: JsonProcessingException) {
      throw SerializationException(e)
    }
}

private class InvalidQualifiedSourceNodeIdException(cause: Throwable) :
  JsonProcessingException(cause)

private object QualifiedSourceNodeIdSerializer :
  StdSerializer<QualifiedSourceNodeId<*>>(QualifiedSourceNodeId::class.java) {

  override fun serialize(
    value: QualifiedSourceNodeId<*>,
    gen: JsonGenerator,
    provider: SerializerProvider?
  ) {
    gen.writeString(value.toString())
  }
}

private object QualifiedSourceNodeIdDeserializer :
  StdDeserializer<QualifiedSourceNodeId<*>>(QualifiedSourceNodeId::class.java) {

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): QualifiedSourceNodeId<*> =
    tryParseQualifiedSourceNodeId(p.valueAsString)
}

private object QualifiedSourceNodeIdKeyDeserializer : KeyDeserializer() {
  override fun deserializeKey(
    key: String,
    ctxt: DeserializationContext?
  ): QualifiedSourceNodeId<*> = tryParseQualifiedSourceNodeId(key)
}

private class InvalidSourceNodeIdException(cause: Throwable) : JsonProcessingException(cause)

private object SourceNodeIdSerializer : StdSerializer<SourceNodeId>(SourceNodeId::class.java) {
  override fun serialize(value: SourceNodeId, gen: JsonGenerator, provider: SerializerProvider?) {
    gen.writeString(value.toString())
  }
}

private object SourcePathDeserializer : StdDeserializer<SourcePath>(SourcePath::class.java) {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): SourcePath =
    tryParseId(p.valueAsString, ::SourcePath)
}

private object IdentifierDeserializer : StdDeserializer<Identifier>(Identifier::class.java) {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Identifier =
    tryParseId(p.valueAsString, ::Identifier)
}

private object SignatureDeserializer : StdDeserializer<Signature>(Signature::class.java) {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Signature =
    tryParseId(p.valueAsString, ::Signature)
}

private object RevisionIdSerializer : StdSerializer<RevisionId>(RevisionId::class.java) {
  override fun serialize(value: RevisionId, gen: JsonGenerator, provider: SerializerProvider?) {
    gen.writeString(value.toString())
  }
}

private object RevisionIdDeserializer : StdDeserializer<RevisionId>(RevisionId::class.java) {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): RevisionId =
    tryParseId(p.valueAsString, ::RevisionId)
}

private object RepositoryIdSerializer : StdSerializer<RepositoryId>(RepositoryId::class.java) {
  override fun serialize(value: RepositoryId, gen: JsonGenerator, provider: SerializerProvider?) {
    gen.writeString(value.toString())
  }
}

private object RepositoryIdDeserializer : StdDeserializer<RepositoryId>(RepositoryId::class.java) {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): RepositoryId =
    tryParseId(p.valueAsString, RepositoryId::parseFrom)
}

private fun tryParseQualifiedSourceNodeId(rawQualifiedId: String): QualifiedSourceNodeId<*> =
  try {
    QualifiedSourceNodeId.parseFrom(rawQualifiedId)
  } catch (e: IllegalArgumentException) {
    throw InvalidQualifiedSourceNodeIdException(e)
  }

private fun <T> tryParseId(rawId: String, builder: (String) -> T): T =
  try {
    builder(rawId)
  } catch (e: IllegalArgumentException) {
    throw InvalidSourceNodeIdException(e)
  }
