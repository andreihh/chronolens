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

package org.metanalysis.test.core.model

import org.metanalysis.core.model.ProjectEdit
import org.metanalysis.core.model.ProjectEdit.AddNode
import org.metanalysis.core.model.ProjectEdit.RemoveNode
import org.metanalysis.core.repository.Transaction

@ModelBuilderMarker
class TransactionBuilder(private val id: String) {
    var date: Long = System.currentTimeMillis()
    var author: String = "<unknown-author>"
    private val edits = arrayListOf<ProjectEdit>()

    fun addSourceUnit(path: String, init: UnitBuilder.() -> Unit) {
        edits += AddNode(sourceUnit(path, init))
    }

    fun addType(id: String, init: TypeBuilder.() -> Unit) {
        edits += AddNode(type(id, init))
    }

    fun addFunction(id: String, init: FunctionBuilder.() -> Unit) {
        edits += AddNode(function(id, init))
    }

    fun addVariable(id: String, init: VariableBuilder.() -> Unit) {
        edits += AddNode(variable(id, init))
    }

    fun removeNode(id: String) {
        edits += RemoveNode(id)
    }

    fun editType(id: String, init: EditTypeBuilder.() -> Unit) {
        edits += EditTypeBuilder(id).apply(init).build()
    }

    fun editFunction(id: String, init: EditFunctionBuilder.() -> Unit) {
        edits += EditFunctionBuilder(id).apply(init).build()
    }

    fun editVariable(id: String, init: EditVariableBuilder.() -> Unit) {
        edits += EditVariableBuilder(id).apply(init).build()
    }

    fun build(): Transaction = Transaction(id, date, author, edits)
}
