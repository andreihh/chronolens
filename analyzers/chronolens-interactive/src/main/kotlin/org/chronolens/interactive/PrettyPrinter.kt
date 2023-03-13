/*
 * Copyright 2022-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.interactive

import org.chronolens.model.Function
import org.chronolens.model.SourceFile
import org.chronolens.model.SourceNode
import org.chronolens.model.Type
import org.chronolens.model.Variable

internal class PrettyPrinter private constructor(private val out: Appendable) {
  private data class Context(val newIndent: String, val indent: String) {
    fun with(new: String, normal: String): Context =
      Context(newIndent = "$indent$new", indent = "$indent$normal")
  }

  private fun Context.visit(value: String) {
    out.appendLine("$newIndent$value")
  }

  private fun Context.visit(values: Collection<*>) {
    val lastIndex = values.size - 1
    for ((index, value) in values.withIndex()) {
      val new = if (index == lastIndex) "`- " else "|- "
      val normal = if (index == lastIndex) "   " else "|  "
      val context = with(new, normal)
      when (value) {
        is SourceNode -> context.visit(value)
        else -> context.visit(value.toString())
      }
    }
  }

  private fun Context.visit(vararg fields: Pair<String, *>) {
    val presentFields =
      fields.filter { (_, value) ->
        if (value is Collection<*>) value.isNotEmpty() else value != null
      }
    val lastIndex = presentFields.size - 1
    for ((index, field) in presentFields.withIndex()) {
      val (key, value) = field
      val new = if (index == lastIndex) "`- " else "|- "
      val normal = if (index == lastIndex) "   " else "|  "
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
    out.appendLine("${newIndent}file ${file.path}")
    visit(file.entities)
  }

  private fun Context.visit(type: Type) {
    out.appendLine("${newIndent}type ${type.name}")
    visit(
      "supertypes" to type.supertypes,
      "modifiers" to type.modifiers,
      "members" to type.members,
    )
  }

  private fun Context.visit(function: Function) {
    out.appendLine("${newIndent}function ${function.signature}")
    visit(
      "parameters" to function.parameters,
      "modifiers" to function.modifiers,
      "body" to summarizeBlock(function.body),
    )
  }

  private fun Context.visit(variable: Variable) {
    out.appendLine("${newIndent}variable ${variable.name}")
    visit(
      "modifiers" to variable.modifiers,
      "initializer" to summarizeBlock(variable.initializer),
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

  private fun visit(node: SourceNode) {
    Context(newIndent = "", indent = "").visit(node)
  }

  companion object {
    @JvmStatic
    fun stringify(sourceNode: SourceNode): String {
      val out = StringBuilder()
      PrettyPrinter(out).visit(sourceNode)
      return out.toString()
    }
  }
}

private fun summarizeBlock(block: List<String>): String? =
  when (block.size) {
    0 -> null
    1 -> "\"${block[0]}\""
    else -> "\"${block[0]}\" ... (trimmed)"
  }
