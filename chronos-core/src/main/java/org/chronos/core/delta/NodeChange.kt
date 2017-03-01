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
import org.chronos.core.Node.Function
import org.chronos.core.Node.Type
import org.chronos.core.Node.Variable
import org.chronos.core.delta.Change.Companion.apply

sealed class NodeChange {
    companion object {
        internal fun Set<Node>.apply(changes: List<NodeChange>): Set<Node> {
            val types = hashMapOf<String, Type>()
            val variables = hashMapOf<String, Variable>()
            val functions = hashMapOf<String, Function>()
            forEach { node ->
                when (node) {
                    is Type -> types[node.name] = node
                    is Variable -> variables[node.name] = node
                    is Function -> functions[node.signature] = node
                }
            }
            changes.forEach { change ->
                when (change) {
                    is AddType -> types[change.type.name] = change.type
                    is AddVariable ->
                        variables[change.variable.name] = change.variable
                    is AddFunction ->
                        functions[change.function.signature] = change.function
                    is RemoveType -> types.remove(change.name)
                    is RemoveVariable -> variables.remove(change.name)
                    is RemoveFunction -> functions.remove(change.signature)
                    is ChangeType -> types[change.name] = checkNotNull(
                            types[change.name]
                    ).apply(change.typeChange)
                    is ChangeVariable -> variables[change.name] = checkNotNull(
                            variables[change.name]
                    ).apply(change.variableChange)
                    is ChangeFunction -> functions[change.signature] = checkNotNull(
                            functions[change.signature]
                    ).apply(change.functionChange)
                }
            }
            val members = types.values + variables.values + functions.values
            return members.toSet()
        }
    }

    data class AddType(val type: Type) : NodeChange()

    data class AddVariable(val variable: Variable) : NodeChange()

    data class AddFunction(val function: Function) : NodeChange()

    data class RemoveType(val name: String) : NodeChange()

    data class RemoveVariable(val name: String) : NodeChange()

    data class RemoveFunction(val signature: String) : NodeChange()

    data class ChangeType(
            val name: String,
            val typeChange: TypeChange
    ) : NodeChange()

    data class ChangeVariable(
            val name: String,
            val variableChange: VariableChange
    ) : NodeChange()

    data class ChangeFunction(
            val signature: String,
            val functionChange: FunctionChange
    ) : NodeChange()
}
