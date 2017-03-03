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
import org.chronos.core.delta.FunctionChange.ParameterChange.AddParameter
import org.chronos.core.delta.FunctionChange.ParameterChange.RemoveParameter

/**
 * A change which should be applied on a [Function].
 *
 * @property parameterChanges the list of changes which should be applied to the
 * `parameters`
 * @property bodyChange the change which should be applied to the `body`, or
 * `null` if the `body` shouldn't be changed
 */
data class FunctionChange(
        val parameterChanges: List<ParameterChange> = emptyList(),
        val bodyChange: BlockChange? = null
) : Change<Function> {
    /** A changed which should be applied to a list of function parameters. */
    sealed class ParameterChange {
        /**
         * Indicates that a parameter should be added in a parameter list.
         *
         * @property index the position in the parameter list where this
         * parameter should be added
         * @property variable the added parameter
         */
        data class AddParameter(
                val index: Int,
                val variable: Variable
        ) : ParameterChange()

        /**
         * Indicates that a parameter should be removed from a parameter list.
         *
         * @property index the position in the parameter list of the parameter
         * which should be removed
         */
        data class RemoveParameter(val index: Int) : ParameterChange()
    }

    override fun applyOn(subject: Function): Function {
        val parameters = subject.parameters.toMutableList()
        parameterChanges.forEach { change ->
            when (change) {
                is AddParameter -> parameters.add(change.index, change.variable)
                is RemoveParameter -> parameters.removeAt(change.index)
            }
        }
        val body =
                if (bodyChange != null) subject.body.apply(bodyChange)
                else subject.body
        return subject.copy(parameters = parameters, body = body)
    }
}
