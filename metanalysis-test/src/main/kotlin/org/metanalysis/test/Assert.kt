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

import org.metanalysis.core.delta.FunctionTransaction
import org.metanalysis.core.delta.NodeSetEdit
import org.metanalysis.core.delta.SourceFileTransaction
import org.metanalysis.core.delta.Transaction
import org.metanalysis.core.delta.TypeTransaction
import org.metanalysis.core.delta.VariableTransaction
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
        expected: NodeSetEdit?,
        actual: NodeSetEdit?,
        message: String? = null
) {
    assertEqualsKt(expected, actual, message)
    when (expected) {
        is NodeSetEdit.Add -> {
            assertEqualsKt(expected, actual as NodeSetEdit.Add, message)
            assertEquals(expected.node, actual.node, message)
        }
        is NodeSetEdit.Remove ->
            assertEqualsKt(expected, actual as NodeSetEdit.Remove, message)
        is NodeSetEdit.Change<*> -> {
            assertEqualsKt(expected, actual as NodeSetEdit.Change<*>, message)
            assertEquals(expected.transaction, actual.transaction)
        }
    }
}

fun assertEquals(
        expected: TypeTransaction?,
        actual: TypeTransaction?,
        message: String? = null
) {
    assertEqualsKt(expected, actual, message)
    if (expected != null && actual != null) {
        val memberEdits = expected.memberEdits.zip(actual.memberEdits)
        memberEdits.forEach { (expectedEdit, actualEdit) ->
            assertEquals(expectedEdit, actualEdit)
        }
    }
}

fun assertEquals(
        expected: FunctionTransaction?,
        actual: FunctionTransaction?,
        message: String? = null
) {
    assertEqualsKt(expected, actual, message)
    if (expected != null && actual != null) {
        val parameterEdits = expected.parameterEdits.zip(actual.parameterEdits)
        parameterEdits.forEach { (expectedEdit, actualEdit) ->
            //TODO
        }
    }
}

fun assertEquals(
        expected: VariableTransaction?,
        actual: VariableTransaction?,
        message: String? = null
) {
    assertEqualsKt(expected, actual, message)
}

fun assertEquals(
        expected: Transaction<out Node>?,
        actual: Transaction<out Node>?,
        message: String? = null
) {
    assertEqualsKt(expected, actual, message)
    when (expected) {
        is TypeTransaction ->
            assertEquals(expected, actual as TypeTransaction, message)
        is FunctionTransaction ->
            assertEquals(expected, actual as FunctionTransaction, message)
        is VariableTransaction ->
            assertEquals(expected, actual as VariableTransaction, message)
    }
}

fun assertEquals(
        expected: SourceFileTransaction?,
        actual: SourceFileTransaction?,
        message: String? = null
) {
    assertEqualsKt(expected, actual, message)
    if (expected != null && actual != null) {
        assertEqualsKt(expected.nodeEdits, actual.nodeEdits, message)
        val nodeEdits = expected.nodeEdits.zip(actual.nodeEdits)
        nodeEdits.forEach { (expectedEdit, actualEdit) ->
            assertEquals(expectedEdit, actualEdit, message)
        }
    }
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
            val expectedHistory = expected.getFileHistory(path)
            val actualHistory = actual.getFileHistory(path)
            assertEqualsKt(expectedHistory, actualHistory, message)
            val history = expectedHistory.zip(actualHistory)
            history.forEach { (expectedEntry, actualEntry) ->
                assertEquals(
                        expected = expectedEntry.transaction,
                        actual = actualEntry.transaction,
                        message = message
                )
            }
        }
    }
}
