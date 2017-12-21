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

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.metanalysis.core.model.Edit
import org.metanalysis.core.model.ListEdit
import org.metanalysis.core.model.ProjectEdit.AddNode
import org.metanalysis.core.model.ProjectEdit.EditFunction
import org.metanalysis.core.model.ProjectEdit.EditType
import org.metanalysis.core.model.ProjectEdit.EditVariable
import org.metanalysis.core.model.ProjectEdit.RemoveNode
import org.metanalysis.core.model.SetEdit
import org.metanalysis.core.model.SourceNode
import org.metanalysis.core.model.SourceNode.SourceEntity.Function
import org.metanalysis.core.model.SourceNode.SourceEntity.Type
import org.metanalysis.core.model.SourceNode.SourceEntity.Variable
import org.metanalysis.core.model.SourceNode.SourceUnit
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Provides JSON serialization and deserialization of arbitrary objects. */
object JsonModule {
    private val typeIdResolver = object : TypeIdResolverBase() {
        private val typeToId = mapOf(
                SourceUnit::class.java to "SourceUnit",
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
        setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        setDefaultTyping(typeResolver)
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    /**
     * Serializes the given object to the given stream.
     *
     * @param out the stream to which the object is serialized
     * @param value the object which should be serialized
     * @throws JsonException if there are any serialization errors
     * @throws IOException if there are any output related errors
     */
    @Throws(IOException::class)
    @JvmStatic
    fun serialize(out: OutputStream, value: Any) {
        try {
            objectMapper.writeValue(out, value)
        } catch (e: JsonProcessingException) {
            throw JsonException(e)
        }
    }

    /**
     * Deserializes an object of the given `type` from the given stream.
     *
     * @param T the type of the deserialized object; must not be generic
     * @param src the stream from which the object is deserialized
     * @param type the class object of the deserialized object
     * @return the deserialized object
     * @throws JsonException if there are any deserialization errors
     * @throws IOException if there are any input related errors
     */
    @Throws(IOException::class)
    @JvmStatic
    fun <T : Any> deserialize(src: InputStream, type: Class<T>): T = try {
        objectMapper.readValue(src, type)
    } catch (e: JsonProcessingException) {
        throw JsonException(e)
    }

    /** Inline utility method. */
    @Throws(IOException::class)
    inline fun <reified T : Any> deserialize(src: InputStream): T =
            deserialize(src, T::class.java)
}
