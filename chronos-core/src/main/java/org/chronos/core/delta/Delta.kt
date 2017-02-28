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

fun String?.apply(changes: List<BlockChange>): String? =
        changes.fold(this) { block, change ->
            when (change) {
                is BlockChange.Set -> change.statements
            }
        }

fun String?.apply(vararg changes: BlockChange): String? =
        apply(changes.asList())
