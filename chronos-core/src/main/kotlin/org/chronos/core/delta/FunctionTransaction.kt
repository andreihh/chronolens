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
import org.chronos.core.delta.BlockEdit.Companion.apply
import org.chronos.core.delta.BlockEdit.Companion.diff
import org.chronos.core.delta.ListEdit.Companion.apply
import org.chronos.core.delta.ListEdit.Companion.diff

/**
 * A transaction which should be applied on a [Function].
 *
 * @property parameterEdits the edits which should be applied to the
 * `parameters`
 * @property bodyEdit the edit which should be applied to the `body`, or `null`
 * if the `body` shouldn't be changed
 */
data class FunctionTransaction(
        val parameterEdits: List<ListEdit<Variable>> = emptyList(),
        val bodyEdit: BlockEdit? = null
) : Transaction<Function> {
    companion object {
        /**
         * Returns the transaction which should be applied on this function to
         * obtain the `other` function, or `null` if they are identical.
         *
         * @param other the function which should be obtained
         * @return the transaction which should be applied on this function
         * @throws IllegalArgumentException if the given functions have
         * different identifiers
         */
        @JvmStatic fun Function.diff(other: Function): FunctionTransaction? {
            require(identifier == other.identifier)
            val parameterEdits = parameters.diff(other.parameters)
            val bodyEdit = body.diff(other.body)
            return if (parameterEdits.isNotEmpty() || bodyEdit != null)
                FunctionTransaction(parameterEdits, bodyEdit)
            else null
        }
    }

    override fun applyOn(subject: Function): Function = subject.copy(
            parameters = subject.parameters.apply(parameterEdits),
            body = subject.body.apply(bodyEdit)
    )
}
