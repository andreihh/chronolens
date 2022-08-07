/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.model

import org.chronolens.core.model.ListEdit.Companion.diff
import org.chronolens.core.model.SetEdit.Companion.diff

/**
 * Returns the smallest edit distance between the two given arrays using the Wagner-Fischer
 * algorithm.
 */
internal fun diff(a: IntArray, b: IntArray): List<ListEdit<Int>> {
    val dp = Array(size = a.size + 1) { IntArray(size = b.size + 1) { a.size + b.size } }
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
 * Returns the edit which must be applied on this source node in order to obtain the [other] source
 * node, or `null` if the two nodes are equal (child nodes are not taken into account).
 *
 * @throws IllegalArgumentException if the two source nodes have different ids or types
 */
internal fun SourceTreeNode<*>.diff(other: SourceTreeNode<*>): SourceTreeEdit? {
    require(qualifiedId == other.qualifiedId) {
        "Can't compute diff between '$qualifiedId' and '${other.qualifiedId}'!"
    }
    val editBuilder =
        when (sourceNode) {
            is SourceFile -> { _ -> null }
            is Type -> sourceNode.diff(other.sourceNode as Type)
            is Function -> sourceNode.diff(other.sourceNode as Function)
            is Variable -> sourceNode.diff(other.sourceNode as Variable)
        }
    return editBuilder.invoke(qualifiedId.cast())
}

private typealias SourceTreeEditBuilder<T> = (QualifiedSourceNodeId<T>) -> SourceTreeEdit?

private fun Type.diff(other: Type): SourceTreeEditBuilder<Type> {
    val supertypeEdits = supertypes.diff(other.supertypes)
    val modifierEdits = modifiers.diff(other.modifiers)
    val changed = supertypeEdits.isNotEmpty() || modifierEdits.isNotEmpty()
    return { qualifiedId ->
        if (!changed) null else EditType(qualifiedId, supertypeEdits, modifierEdits)
    }
}

private fun Function.diff(other: Function): SourceTreeEditBuilder<Function> {
    val parameterEdits = parameters.diff(other.parameters)
    val modifierEdits = modifiers.diff(other.modifiers)
    val bodyEdits = body.diff(other.body)
    val changed =
        parameterEdits.isNotEmpty() || modifierEdits.isNotEmpty() || bodyEdits.isNotEmpty()
    return { qualifiedId ->
        if (!changed) null else EditFunction(qualifiedId, parameterEdits, modifierEdits, bodyEdits)
    }
}

private fun Variable.diff(other: Variable): SourceTreeEditBuilder<Variable> {
    val modifierEdits = modifiers.diff(other.modifiers)
    val initializerEdits = initializer.diff(other.initializer)
    val changed = modifierEdits.isNotEmpty() || initializerEdits.isNotEmpty()
    return { qualifiedId ->
        if (!changed) null else EditVariable(qualifiedId, modifierEdits, initializerEdits)
    }
}
