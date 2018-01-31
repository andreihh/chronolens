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

@file:JvmName("Utils")
@file:JvmMultifileClass

package org.metanalysis.core.model

import org.metanalysis.core.model.ListEdit.Companion.diff
import org.metanalysis.core.model.SetEdit.Companion.diff

/**
 * Returns the smallest edit distance between the two given arrays using the
 * Wagner-Fischer algorithm.
 */
internal fun diff(a: IntArray, b: IntArray): List<ListEdit<Int>> {
    val dp = Array(size = a.size + 1) {
        IntArray(size = b.size + 1) { a.size + b.size }
    }
    dp[0] = IntArray(size = b.size + 1) { it }
    for (i in 1..a.size) {
        dp[i][0] = i
        for (j in 1..b.size) {
            dp[i][j] =
                if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                else 1 + minOf(dp[i - 1][j], dp[i][j - 1])
        }
    }
    val edits = arrayListOf<ListEdit<Int>>()
    var i = a.size
    var j = b.size
    while (i > 0 || j > 0) {
        if (i > 0 && j > 0 && a[i - 1] == b[j - 1]) {
            i--
            j--
        } else if (i > 0 && dp[i][j] == dp[i - 1][j] + 1) {
            edits += ListEdit.Remove(i - 1)
            i--
        } else {
            edits += ListEdit.Add(i, b[j - 1])
            j--
        }
    }
    return edits
}

/**
 * Returns the edit which must be applied on this source node in order to obtain
 * the [other] source node, or `null` if the two nodes are equal.
 *
 * @throws IllegalArgumentException if the two source nodes have different ids
 */
internal fun SourceNode.diff(other: SourceNode): ProjectEdit? {
    require(id == other.id) {
        "Can't compute diff between '$id' and '${other.id}'!"
    }
    return when (this) {
        is SourceUnit -> null
        is Type -> diff(other as Type)
        is Function -> diff(other as Function)
        is Variable -> diff(other as Variable)
    }
}

private fun Type.diff(other: Type): EditType? {
    val modifierEdits = modifiers.diff(other.modifiers)
    val supertypeEdits = supertypes.diff(other.supertypes)
    val edit = EditType(id, modifierEdits, supertypeEdits)
    val isChanged = modifierEdits.isNotEmpty() || supertypeEdits.isNotEmpty()
    return if (isChanged) edit else null
}

private fun Function.diff(other: Function): EditFunction? {
    val modifierEdits = modifiers.diff(other.modifiers)
    val parameterNames = parameters.map(Variable::name)
    val otherParameterNames = other.parameters.map(Variable::name)
    val parameterEdits = parameterNames.diff(otherParameterNames)
    val bodyEdits = body.diff(other.body)
    val edit = EditFunction(id, modifierEdits, parameterEdits, bodyEdits)
    val isChanged = modifierEdits.isNotEmpty()
        || parameterEdits.isNotEmpty()
        || bodyEdits.isNotEmpty()
    return if (isChanged) edit else null
}

private fun Variable.diff(other: Variable): EditVariable? {
    val modifierEdits = modifiers.diff(other.modifiers)
    val initializerEdits = initializer.diff(other.initializer)
    val edit = EditVariable(id, modifierEdits, initializerEdits)
    val isChanged = modifierEdits.isNotEmpty() || initializerEdits.isNotEmpty()
    return if (isChanged) edit else null
}
