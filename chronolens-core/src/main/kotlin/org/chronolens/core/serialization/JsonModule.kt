/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.chronolens.core.model.AddNode
import org.chronolens.core.model.Edit
import org.chronolens.core.model.EditFunction
import org.chronolens.core.model.EditType
import org.chronolens.core.model.EditVariable
import org.chronolens.core.model.Function
import org.chronolens.core.model.ListEdit
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.model.SetEdit
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceNode
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Provides JSON serialization and deserialization of arbitrary objects. */
public object JsonModule {
    private val typeIdResolver = object : TypeIdResolverBase() {
        private val typeToId = mapOf(
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

        override fun idFromValueAndType(
            value: Any?,
            suggestedType: Class<*>
        ): String = typeToId.getValue(suggestedType)

        override fun idFromValue(value: Any): String =
            typeToId.getValue(value.javaClass)

        override fun typeFromId(
            context: DatabindContext,
            id: String
        ): JavaType = context.typeFactory.constructType(idToType.getValue(id))

        override fun getMechanism(): JsonTypeInfo.Id = JsonTypeInfo.Id.NAME
    }.apply { init(null) }

    private val typeResolver =
        object : DefaultTypeResolverBuilder(OBJECT_AND_NON_CONCRETE) {
            private val abstractTypes =
                listOf(SourceNode::class.java, Edit::class.java)

            override fun useForType(t: JavaType): Boolean =
                abstractTypes.any { it.isAssignableFrom(t.rawClass) }
        }.init(JsonTypeInfo.Id.NAME, typeIdResolver)
            .inclusion(JsonTypeInfo.As.PROPERTY)
            .typeProperty("@class")

    private val objectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        setDefaultTyping(typeResolver)
        disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    /**
     * Serializes the given [value] object to the given [out] stream.
     *
     * @throws JsonException if there are any serialization errors
     * @throws IOException if there are any output related errors
     */
    @Throws(IOException::class)
    @JvmStatic
    public fun serialize(out: OutputStream, value: Any) {
        try {
            objectMapper.writeValue(out, value)
        } catch (e: JsonProcessingException) {
            throw JsonException(e)
        }
    }

    /**
     * Deserializes an object of the given non-generic [type] from the given
     * [src] stream.
     *
     * @throws JsonException if there are any deserialization errors
     * @throws IOException if there are any input related errors
     */
    @Throws(IOException::class)
    @JvmStatic
    public fun <T : Any> deserialize(
        src: InputStream,
        type: Class<T>,
    ): T = try {
        objectMapper.readValue(src, type)
    } catch (e: JsonProcessingException) {
        throw JsonException(e)
    }

    /** Inline utility method. */
    @Throws(IOException::class)
    @JvmStatic
    public inline fun <reified T : Any> deserialize(src: InputStream): T =
        deserialize(src, T::class.java)
}
