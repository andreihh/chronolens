/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.core.repository

import org.chronolens.core.model.AddNode
import org.chronolens.core.model.ProjectEdit
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.repository.Transaction
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply
import org.chronolens.test.core.model.EditFunctionBuilder
import org.chronolens.test.core.model.EditTypeBuilder
import org.chronolens.test.core.model.EditVariableBuilder
import org.chronolens.test.core.model.FunctionBuilder
import org.chronolens.test.core.model.SourceFileBuilder
import org.chronolens.test.core.model.TypeBuilder
import org.chronolens.test.core.model.VariableBuilder
import org.chronolens.test.core.model.function
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.model.type
import org.chronolens.test.core.model.variable

class TransactionBuilder(private val revisionId: String) {
    var date: Long = System.currentTimeMillis()
    var author: String = "<unknown-author>"
    private val edits = mutableListOf<ProjectEdit>()

    fun date(value: Long): TransactionBuilder {
        date = value
        return this
    }

    fun author(value: String): TransactionBuilder {
        author = value
        return this
    }

    fun addSourceFile(
        path: String,
        init: Init<SourceFileBuilder>
    ): TransactionBuilder {
        edits += AddNode(sourceFile(path, init))
        return this
    }

    fun addType(id: String, init: Init<TypeBuilder>): TransactionBuilder {
        edits += AddNode(type(id, init))
        return this
    }

    fun addFunction(
        id: String,
        init: Init<FunctionBuilder>
    ): TransactionBuilder {
        edits += AddNode(function(id, init))
        return this
    }

    fun addVariable(
        id: String,
        init: Init<VariableBuilder>
    ): TransactionBuilder {
        edits += AddNode(variable(id, init))
        return this
    }

    fun removeNode(id: String): TransactionBuilder {
        edits += RemoveNode(id)
        return this
    }

    fun editType(id: String, init: Init<EditTypeBuilder>): TransactionBuilder {
        edits += EditTypeBuilder(id).apply(init).build()
        return this
    }

    fun editFunction(
        id: String,
        init: Init<EditFunctionBuilder>
    ): TransactionBuilder {
        edits += EditFunctionBuilder(id).apply(init).build()
        return this
    }

    fun editVariable(
        id: String,
        init: Init<EditVariableBuilder>
    ): TransactionBuilder {
        edits += EditVariableBuilder(id).apply(init).build()
        return this
    }

    fun build(): Transaction = Transaction(revisionId, date, author, edits)
}
