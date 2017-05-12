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

package org.metanalysis.test

import org.metanalysis.core.model.Node
import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.project.Project

import kotlin.test.assertEquals as assertEqualsKt
import kotlin.test.assertNotNull
import kotlin.test.assertNull

fun assertEquals(expected: Type?, actual: Type?, message: String? = null) {
    assertEqualsKt(expected?.name, actual?.name, message)
    assertEqualsKt(expected?.supertypes, actual?.supertypes, message)
    assertEqualsKt(expected?.modifiers, actual?.modifiers, message)
    assertEquals(expected?.members, actual?.members, message)
}

fun assertEquals(
        expected: Variable?,
        actual: Variable?,
        message: String? = null
) {
    assertEqualsKt(expected, actual, message)
    assertEqualsKt(expected?.name, actual?.name, message)
    assertEqualsKt(expected?.modifiers, actual?.modifiers, message)
    assertEqualsKt(expected?.initializer, actual?.initializer, message)
}

fun assertEquals(
        expected: Function?,
        actual: Function?,
        message: String? = null
) {
    assertEqualsKt(expected, actual, message)
    if (expected != null && actual != null) {
        assertEqualsKt(expected.signature, actual.signature, message)
        assertEqualsKt(expected.parameters, actual.parameters, message)
        val parameters = expected.parameters.zip(actual.parameters)
        parameters.forEach { (expectedParameter, actualParameter) ->
            assertEquals(expectedParameter, actualParameter, message)
        }
        assertEqualsKt(expected.modifiers, actual.modifiers, message)
        assertEqualsKt(expected.body, actual.body, message)
    }
}

fun assertEquals(expected: Node?, actual: Node?, message: String? = null) {
    assertEqualsKt(expected, actual, message)
    if (expected != null && actual != null) {
        assertEqualsKt(expected::class, actual::class, message)
        when {
            expected is Type && actual is Type -> assertEquals(expected, actual)
            expected is Variable && actual is Variable ->
                assertEquals(expected, actual)
            expected is Function && actual is Function ->
                assertEquals(expected, actual)
            else -> throw AssertionError("Invalid node types!")
        }
    }
}

fun assertEquals(
        expected: Set<Node>?,
        actual: Set<Node>?,
        message: String? = null
) {
    assertEqualsKt(expected, actual, message)
    if (expected != null && actual != null) {
        val actualSourceFile = SourceFile(actual)
        expected.forEach { expectedNode ->
            val actualNode = actualSourceFile
                    .find(expectedNode::class, expectedNode.identifier)
            assertNotNull(actualNode, message)
            assertEquals(expectedNode, actualNode as Node, message)
        }
    }
}

fun assertEquals(
        expected: SourceFile?,
        actual: SourceFile?,
        message: String? = null
) {
    assertEqualsKt(expected, actual, message)
    assertEquals(expected?.nodes, actual?.nodes, message)
}

fun assertEquals(
        expected: Project?,
        actual: Project?,
        message: String? = null
) {
    if (expected == null) {
        assertNull(actual, message)
    }
    if (expected != null && actual != null) {
        val expectedFiles = expected.listFiles()
        val actualFiles = actual.listFiles()
        assertEqualsKt(expectedFiles, actualFiles, message)
        expectedFiles.forEach { path ->
            assertEquals(
                    expected = expected.getFileModel(path),
                    actual = actual.getFileModel(path),
                    message = message
            )
            assertEqualsKt(
                    expected = expected.getFileHistory(path),
                    actual = actual.getFileHistory(path),
                    message = message
            )
        }
    }
}
