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

@file:JvmName("Delta")

package org.chronos.core.delta

import org.chronos.core.Node
import org.chronos.core.Node.Function
import org.chronos.core.Node.Type
import org.chronos.core.Node.Variable
import org.chronos.core.SourceFile
import org.chronos.core.delta.FunctionChange.ParameterChange
import org.chronos.core.delta.TypeChange.SupertypeChange

fun SourceFile.diff(other: SourceFile): SourceFileChange = TODO()

fun SourceFile.apply(changes: List<SourceFileChange>): SourceFile = this

fun SourceFile.apply(vararg changes: SourceFileChange): SourceFile = this

fun Type.apply(change: TypeChange): Type = Type(
        name = name,
        supertypes = supertypes.apply(change.supertypeChanges),
        members = members
)

fun Variable.apply(changes: List<VariableChange>): Variable = Variable(
        name,
        if (changes.isEmpty()) initializer else changes.last().initializerChange
)

fun Variable.apply(vararg changes: VariableChange): Variable =
        apply(changes.asList())

fun Function.apply(changes: List<FunctionChange>): Function =
        changes.fold(parameters to body) { f, change ->
            val p = f.first.apply(change.parameterChanges)
            val b = f.second.apply(listOf(change.bodyChange).filterNotNull())
            p to b
        }.let { f -> Function(signature, f.first, f.second) }

fun Function.apply(vararg changes: FunctionChange): Function =
        apply(changes.asList())

fun List<Variable>.apply(changes: List<ParameterChange>): List<Variable> =
        changes.fold(this.toMutableList()) { parameters, change ->
            when (change) {
                is ParameterChange.Add ->
                    parameters.add(change.index, change.variable)
                is ParameterChange.Remove -> parameters.removeAt(change.index)
            }
            parameters
        }

fun List<Variable>.apply(vararg changes: ParameterChange): List<Variable> =
        apply(changes.asList())

fun Set<String>.apply(changes: List<SupertypeChange>): Set<String> =
        changes.fold(this.toMutableSet()) { supertypes, change ->
            when (change) {
                is SupertypeChange.Add -> supertypes.add(change.name)
                is SupertypeChange.Remove -> supertypes.remove(change.name)
            }
            supertypes
        }

fun Set<String>.apply(vararg changes: SupertypeChange): Set<String> =
        apply(changes.asList())

fun String?.apply(changes: List<BlockChange>): String? =
        changes.fold(this) { block, change ->
            when (change) {
                is BlockChange.Set -> change.statements
            }
        }

fun String?.apply(vararg changes: BlockChange): String? =
        apply(changes.asList())
