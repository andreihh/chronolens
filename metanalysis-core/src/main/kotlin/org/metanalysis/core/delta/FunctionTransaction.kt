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

package org.metanalysis.core.delta

import org.metanalysis.core.Node.Function
import org.metanalysis.core.Node.Variable
import org.metanalysis.core.delta.ListEdit.Companion.apply
import org.metanalysis.core.delta.ListEdit.Companion.diff
import org.metanalysis.core.delta.MapEdit.Companion.apply
import org.metanalysis.core.delta.MapEdit.Companion.diff

/**
 * A transaction which should be applied on a [Function].
 *
 * @property parameterEdits the edits which should be applied to the
 * `parameters`
 * @property bodyEdits the edits which should be applied to the `body`
 * @property propertyEdits the edits which should be applied to the `properties`
 */
data class FunctionTransaction(
        val parameterEdits: List<ListEdit<Variable>> = emptyList(),
        val bodyEdits: List<ListEdit<String>> = emptyList(),
        val propertyEdits: List<MapEdit<String, String>> = emptyList()
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
            val bodyEdits = body.diff(other.body)
            val propertyEdits = properties.diff(other.properties)
            val isChanged = parameterEdits.isNotEmpty()
                    || bodyEdits.isNotEmpty()
                    || propertyEdits.isNotEmpty()
            return if (isChanged)
                FunctionTransaction(parameterEdits, bodyEdits, propertyEdits)
            else null
        }
    }

    override fun applyOn(subject: Function): Function = subject.copy(
            parameters = subject.parameters.apply(parameterEdits),
            body = subject.body.apply(bodyEdits),
            properties = subject.properties.apply(propertyEdits)
    )
}
