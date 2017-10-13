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
import org.metanalysis.core.model.ProjectEdit.EditFunction
import org.metanalysis.core.model.ProjectEdit.EditType
import org.metanalysis.core.model.ProjectEdit.EditVariable
import org.metanalysis.core.model.SetEdit.Companion.diff
import org.metanalysis.core.model.SourceNode.SourceEntity.Function
import org.metanalysis.core.model.SourceNode.SourceEntity.Type
import org.metanalysis.core.model.SourceNode.SourceEntity.Variable
import org.metanalysis.core.model.SourceNode.SourceUnit

/**
 * Returns the edit which must be applied on this source node in order to obtain
 * the `other` source node, or `null` if the two nodes are equal.
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
    val isChanged = modifierEdits.isNotEmpty() || supertypeEdits.isNotEmpty()
    return EditType(id, modifierEdits, supertypeEdits)
            .takeIf { isChanged }
}

private fun Function.diff(other: Function): EditFunction? {
    val modifierEdits = modifiers.diff(other.modifiers)
    val parameterNames = parameters.map(Variable::name)
    val otherParameterNames = other.parameters.map(Variable::name)
    val parameterEdits = parameterNames.diff(otherParameterNames)
    val bodyEdits = body.diff(other.body)
    val isChanged = modifierEdits.isNotEmpty()
            || parameterEdits.isNotEmpty()
            || bodyEdits.isNotEmpty()
    return EditFunction(id, modifierEdits, parameterEdits, bodyEdits)
            .takeIf { isChanged }
}

private fun Variable.diff(other: Variable): EditVariable? {
    val modifierEdits = modifiers.diff(other.modifiers)
    val initializerEdits = initializer.diff(other.initializer)
    val isChanged = modifierEdits.isNotEmpty() || initializerEdits.isNotEmpty()
    return EditVariable(id, modifierEdits, initializerEdits)
            .takeIf { isChanged }
}
