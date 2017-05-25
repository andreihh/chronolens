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

package org.metanalysis.test.core.delta

import org.metanalysis.core.delta.FunctionTransaction
import org.metanalysis.core.delta.NodeSetEdit
import org.metanalysis.core.delta.SourceFileTransaction
import org.metanalysis.core.delta.Transaction
import org.metanalysis.core.delta.TypeTransaction
import org.metanalysis.core.delta.VariableTransaction
import org.metanalysis.core.model.Node
import org.metanalysis.test.core.model.assertEquals

import kotlin.test.assertEquals as assertEqualsKt

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
