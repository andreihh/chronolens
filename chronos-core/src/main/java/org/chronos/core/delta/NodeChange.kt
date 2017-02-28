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

import org.chronos.core.Node

sealed class NodeChange {
    class Add(val node: Node) : NodeChange()

    class RemoveType(val name: String) : NodeChange()

    class RemoveVariable(val name: String) : NodeChange()

    class RemoveFunction(val signature: String) : NodeChange()

    class ChangeType(
            val name: String,
            val typeChange: TypeChange
    ) : NodeChange()

    class ChangeVariable(
            val name: String,
            val variableChange: VariableChange
    ) : NodeChange()

    class ChangeFunction(
            val signature: String,
            val functionChange: FunctionChange
    ) : NodeChange()
}
