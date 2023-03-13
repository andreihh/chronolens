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

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock

class SerializationModuleTest {
  @Test
  fun stringify_delegatesToSerialize() {
    val serializationModule =
      mock<SerializationModule> {
        on { serialize(any(), eq(10)) } doAnswer
          {
            val dst = (it.arguments[0] as OutputStream)
            dst.write('1'.code)
            dst.write('0'.code)
          }
        on { stringify(any()) }.thenCallRealMethod()
      }

    assertEquals(expected = "10", serializationModule.stringify(10))
  }

  @Test
  fun stringify_propagatesSerializeError() {
    val error = SerializationException(IOException("Error!"))
    val serializationModule =
      mock<SerializationModule> {
        on { serialize(any(), any()) } doThrow error
        on { stringify(any()) }.thenCallRealMethod()
      }

    assertFailsWith<SerializationException> { serializationModule.stringify(10) }
  }

  @Test
  fun inlineDeserialize_delegatesToAbstractDeserialize() {
    val src = ByteArrayInputStream(ByteArray(10))
    val serializationModule =
      mock<SerializationModule> { on { deserialize(src, String::class.java) } doReturn "10" }

    assertEquals(expected = "10", serializationModule.deserialize(src))
  }

  @Test
  fun inlineDeserialize_propagatesDeserializeError() {
    val error = SerializationException(IOException("Error!"))
    val serializationModule =
      mock<SerializationModule> { on { deserialize<String>(any(), any()) } doThrow error }

    assertFailsWith<SerializationException> {
      serializationModule.deserialize<String>(ByteArrayInputStream(ByteArray(10)))
    }
  }
}
