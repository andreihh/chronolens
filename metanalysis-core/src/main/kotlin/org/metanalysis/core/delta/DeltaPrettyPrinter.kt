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

import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable

import java.io.OutputStream

class DeltaPrettyPrinter(out: OutputStream) {
    private val writer = out.writer()
    private var prefix = ""

    private fun writeLine(line: String) {
        writer.apply {
            append(prefix)
            append(line)
            append('\n')
            flush()
        }
    }

    private fun increaseIndent() {
        prefix += "  "
    }

    private fun decreaseIndent() {
        prefix = prefix.removeSuffix("  ")
    }

    fun visit(edit: NodeSetEdit.Add) {
        writeLine(edit.node.toString())
    }

    fun visit(edit: NodeSetEdit.Remove) {
        writeLine(edit.identifier + " " + edit.nodeType)
    }

    fun visit(transaction: TypeTransaction) {
        writeLine(transaction.supertypeEdits.toString())
        transaction.memberEdits.forEach { visit(it) }
    }

    fun visit(transaction: VariableTransaction) {
        writeLine(transaction.initializerEdits.toString())
    }

    fun visit(transaction: FunctionTransaction) {
        writeLine(transaction.parameterEdits.toString())
        writeLine(transaction.bodyEdits.toString())
    }

    fun visit(edit: NodeSetEdit.Change<*>) {
        writeLine(edit.identifier + " " + edit.nodeType)
        when (edit.nodeType) {
            Type::class -> visit(edit.transaction as TypeTransaction)
            Variable::class -> visit(edit.transaction as VariableTransaction)
            Function::class -> visit(edit.transaction as FunctionTransaction)
        }
    }

    fun visit(edit: NodeSetEdit) {
        when (edit) {
            is NodeSetEdit.Add -> visit(edit)
            is NodeSetEdit.Remove -> visit(edit)
            is NodeSetEdit.Change<*> -> visit(edit)
        }
    }

    fun visit(transaction: SourceFileTransaction) {
        writeLine("SourceFileTransaction")
        transaction.nodeEdits.forEach {
            increaseIndent()
            visit(it)
            decreaseIndent()
        }
    }
}
