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

package org.chronos.core.delta

import org.chronos.core.Node.Function
import org.chronos.core.Node.Variable
import org.chronos.core.delta.FunctionChange.ParameterChange.Add
import org.chronos.core.delta.FunctionChange.ParameterChange.Remove

data class FunctionChange(
        val parameterChanges: List<ParameterChange>,
        val bodyChange: BlockChange?
) : Change<Function> {
    sealed class ParameterChange {
        data class Add(
                val index: Int,
                val variable: Variable
        ) : ParameterChange()

        data class Remove(val index: Int) : ParameterChange()
    }

    override fun applyOn(subject: Function): Function {
        val parameters = subject.parameters.toMutableList()
        parameterChanges.forEach { change ->
            when (change) {
                is Add -> parameters.add(change.index, change.variable)
                is Remove -> parameters.removeAt(change.index)
            }
        }
        val body =
                if (bodyChange != null) subject.body.apply(bodyChange)
                else subject.body
        return subject.copy(parameters = parameters, body = body)
    }
}
