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
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import org.metanalysis.core.Node
import org.metanalysis.core.delta.Edit
import org.metanalysis.core.delta.Transaction

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

import kotlin.reflect.KClass

/** Provides JSON serialization and deserialization of arbitrary objects. */
object JsonDriver {
    private fun <T : Any> serializer(
            serialize: (T, JsonGenerator, SerializerProvider) -> Unit
    ): JsonSerializer<T> = object : JsonSerializer<T>() {
        override fun serialize(
                value: T,
                gen: JsonGenerator,
                serializers: SerializerProvider
        ) {
            serialize(value, gen, serializers)
        }
    }

    private fun <T : Any> deserializer(
            deserialize: (JsonParser) -> T
    ): JsonDeserializer<T> = object : JsonDeserializer<T>() {
        override fun deserialize(
                p: JsonParser,
                ctxt: DeserializationContext?
        ): T = deserialize(p)
    }

    private fun findClass(name: String): KClass<*> = try {
        Class.forName(name).kotlin
    } catch (e: ClassNotFoundException) {
        throw IOException(e)
    }

    private val typeResolver = object : DefaultTypeResolverBuilder(
            OBJECT_AND_NON_CONCRETE
    ) {
        private val abstractTypes = listOf(
                Node::class.java,
                Edit::class.java,
                Transaction::class.java
        )

        override fun useForType(t: JavaType): Boolean =
                abstractTypes.any { it.isAssignableFrom(t.rawClass) }
    }.apply {
        init(JsonTypeInfo.Id.CLASS, null)
        inclusion(JsonTypeInfo.As.PROPERTY)
        typeProperty("@class")
    }

    private val kClassSerializer = serializer<KClass<*>> { value, gen, _ ->
        gen.writeString(value.java.name)
    }

    private val kClassDeserializer = deserializer { p ->
        val name = p.codec.readValue<String>(p, String::class.java)
        findClass(name)
    }

    private val objectMapper = jacksonObjectMapper().apply {
        registerModule(SimpleModule().apply {
            addSerializer(KClass::class.java, kClassSerializer)
            addDeserializer(KClass::class.java, kClassDeserializer)
        })
        setDefaultTyping(typeResolver)
        enable(SerializationFeature.INDENT_OUTPUT)
        setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    }

    /**
     * Serializes the given object to the given stream.
     *
     * @param out the stream to which the object is serialized
     * @param value the object which should be serialized
     * @throws IOException if there are any input-related or serialization
     * errors
     */
    @Throws(IOException::class)
    @JvmStatic fun serialize(out: OutputStream, value: Any) {
        objectMapper.writeValue(out, value)
    }

    /**
     * Deserializes an object of the given `type` from the given stream.
     *
     * @param T the type of the deserialized object; must not be generic
     * @param src the stream from which the object is deserialized
     * @param type the class object of the deserialized object
     * @return the deserialized object
     * @throws IOException if there are any input-related or deserialization
     * errors
     */
    @Throws(IOException::class)
    @JvmStatic fun <T : Any> deserialize(src: InputStream, type: KClass<T>): T =
            objectMapper.readValue(src, type.java)

    /** Utility deserialization method. */
    @Throws(IOException::class)
    @JvmStatic inline fun <reified T : Any> deserialize(src: InputStream): T =
            deserialize(src, T::class)
}
