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

package org.chronolens

import org.chronolens.core.model.Function
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceNode
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import java.io.PrintStream

class PrettyPrinterVisitor(private val out: PrintStream) {
    private data class Context(val newIndent: String, val indent: String) {
        fun with(new: String, normal: String): Context =
            Context(newIndent = "$indent$new", indent = "$indent$normal")
    }

    private fun Context.visit(value: String) {
        out.println("$newIndent$value")
    }

    private fun Context.visit(values: Collection<*>) {
        for ((index, value) in values.withIndex()) {
            val new = if (index == values.size - 1) "`-- " else "|-- "
            val normal = if (index == values.size - 1) "    " else "|   "
            val context = with(new, normal)
            when (value) {
                is String -> context.visit(value)
                is SourceNode -> context.visit(value)
            }
        }
    }

    private fun Context.visit(vararg fields: Pair<String, *>) {
        for ((index, field) in fields.withIndex()) {
            val (key, value) = field
            val new = if (index == fields.size - 1) "`-- " else "|-- "
            val normal = if (index == fields.size - 1) "    " else "|   "
            val context = with(new, normal)
            when (value) {
                is String -> context.visit("$key: $value")
                is Collection<*> -> {
                    context.visit("$key:")
                    context.visit(value)
                }
            }
        }
    }

    private fun Context.visit(file: SourceFile) {
        out.println("${newIndent}file ${file.path}")
        visit(file.entities)
    }

    private fun Context.visit(type: Type) {
        out.println("${newIndent}type ${type.name}")
        visit(
            "supertypes" to type.supertypes,
            "modifiers" to type.modifiers,
            "members" to type.members
        )
    }

    private fun Context.visit(function: Function) {
        out.println("${newIndent}function ${function.signature}")
        visit(
            "parameters" to function.parameters,
            "modifiers" to function.modifiers,
            "body" to when (function.body.size) {
                0 -> "<none>"
                1 -> "\"${function.body[0]}\""
                else -> "\"${function.body[0]}\" ... (trimmed)"
            }
        )
    }

    private fun Context.visit(variable: Variable) {
        out.println("${newIndent}variable ${variable.name}")
        visit(
            "modifiers" to variable.modifiers,
            "initializer" to when (variable.initializer.size) {
                0 -> "<none>"
                1 -> "\"${variable.initializer[0]}\""
                else -> "\"${variable.initializer[0]}\" ... (trimmed)"
            }
        )
    }

    private fun Context.visit(node: SourceNode) {
        when (node) {
            is SourceFile -> visit(node)
            is Type -> visit(node)
            is Function -> visit(node)
            is Variable -> visit(node)
        }
    }

    fun visit(node: SourceNode) {
        Context(newIndent = "", indent = "").visit(node)
    }
}
