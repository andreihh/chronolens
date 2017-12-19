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

@file:JvmName("EditBuilders")

package org.metanalysis.test.core.model

import org.metanalysis.core.model.ProjectEdit.AddNode
import org.metanalysis.core.model.ProjectEdit.EditFunction
import org.metanalysis.core.model.ProjectEdit.EditType
import org.metanalysis.core.model.ProjectEdit.EditVariable
import org.metanalysis.core.model.ProjectEdit.RemoveNode
import org.metanalysis.core.repository.Transaction

inline fun transaction(
        id: String,
        init: TransactionBuilder.() -> Unit
): Transaction = TransactionBuilder(id).apply(init).build()

inline fun addSourceUnit(path: String, init: UnitBuilder.() -> Unit): AddNode =
        AddNode(sourceUnit(path, init))

inline fun addType(id: String, init: TypeBuilder.() -> Unit): AddNode =
        AddNode(type(id, init))

inline fun addFunction(id: String, init: FunctionBuilder.() -> Unit): AddNode =
        AddNode(function(id, init))

inline fun addVariable(id: String, init: VariableBuilder.() -> Unit): AddNode =
        AddNode(variable(id, init))

fun removeNode(id: String): RemoveNode = RemoveNode(id)

inline fun editType(id: String, init: EditTypeBuilder.() -> Unit): EditType =
        EditTypeBuilder(id).apply(init).build()

inline fun editFunction(
        id: String,
        init: EditFunctionBuilder.() -> Unit
): EditFunction = EditFunctionBuilder(id).apply(init).build()

inline fun editVariable(
        id: String,
        init: EditVariableBuilder.() -> Unit
): EditVariable = EditVariableBuilder(id).apply(init).build()
