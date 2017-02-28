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

@file:JvmName("Assert")

package org.chronos.test

import org.chronos.core.Node
import org.chronos.core.Node.Function
import org.chronos.core.Node.Type
import org.chronos.core.Node.Variable
import org.chronos.core.SourceFile

import kotlin.test.fail
import kotlin.test.assertEquals as assertEqualsKt

fun assertEquals(
        expected: SourceFile,
        actual: SourceFile,
        message: String? = null
) {
    assertEqualsKt(expected.nodes.size, actual.nodes.size, message)
    for ((en, an) in expected.nodes zip actual.nodes) {
        assertEquals(en, an)
    }
}

fun assertEquals(expected: Node, actual: Node, message: String? = null) {
    when {
        expected is Type && actual is Type -> assertEquals(expected, actual)
        expected is Variable && actual is Variable ->
            assertEquals(expected, actual)
        expected is Function && actual is Function ->
            assertEquals(expected, actual)
        else -> fail(message)
    }
}

fun assertEquals(expected: Type, actual: Type, message: String? = null) {
    assertEqualsKt(expected.name, actual.name, message)
    assertEqualsKt(expected.supertypes, actual.supertypes, message)
    assertEqualsKt(expected.members.size, actual.members.size, message)
    for ((em, am) in (expected.members zip actual.members)) {
        assertEquals(em, am, message)
    }
}

fun assertEquals(
        expected: Variable,
        actual: Variable,
        message: String? = null
) {
    assertEqualsKt(expected.name, actual.name, message)
    assertEqualsKt(expected.initializer, actual.initializer, message)
}

fun assertEquals(
        expected: Function,
        actual: Function,
        message: String? = null
) {
    assertEqualsKt(expected.signature, actual.signature, message)
    assertEqualsKt(
            expected.parameters.size,
            actual.parameters.size,
            message
    )
    for ((ep, ap) in (expected.parameters zip actual.parameters)) {
        assertEquals(ep, ap, message)
    }
    assertEqualsKt(expected.body, actual.body, message)
}
