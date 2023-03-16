/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.api.parsing

import javax.script.ScriptEngineManager
import javax.script.ScriptException
import org.chronolens.api.parsing.Parser
import org.chronolens.api.parsing.SyntaxErrorException
import org.chronolens.model.Function
import org.chronolens.model.Identifier
import org.chronolens.model.SourceEntity
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.chronolens.model.Type
import org.chronolens.model.Variable

public class FakeParser : Parser {
  private val engine =
    ScriptEngineManager().getEngineByExtension("kts").apply {
      eval("import org.chronolens.test.model.sourceFile")
      eval("import org.chronolens.test.model.type")
      eval("import org.chronolens.test.model.variable")
      eval("import org.chronolens.test.model.function")
    }
      ?: throw AssertionError("Must support 'kts' scripts!")

  override fun canParse(path: SourcePath): Boolean = path.toString().endsWith(".kts")

  @Throws(SyntaxErrorException::class)
  override fun parse(path: SourcePath, rawSource: String): SourceFile {
    if (!canParse(path)) {
      throw SyntaxErrorException("Cannot parse '$path'!")
    }
    val result =
      try {
        engine.eval(rawSource)
      } catch (e: ScriptException) {
        throw SyntaxErrorException(e)
      }
    val sourceFile =
      result as? SourceFile
        ?: throw SyntaxErrorException("Parsed object '$result' is not a source file!")
    check(sourceFile.path == path) { "Parsed invalid path '${sourceFile.path}'!" }
    return sourceFile
  }

  @Throws(SyntaxErrorException::class)
  public fun unparse(sourceFile: SourceFile): String {
    if (!canParse(sourceFile.path)) {
      throw SyntaxErrorException("Cannot unparse '${sourceFile.path}'!")
    }
    val rawSource = StringBuilder()
    rawSource.appendLine("sourceFile(\"${sourceFile.path}\") {")
    if (sourceFile.entities.isNotEmpty()) {
      rawSource.append(unparseChildren(sourceFile.entities, "  "))
    }
    rawSource.append("}")
    return rawSource.toString()
  }

  private fun unparseEntity(entity: SourceEntity, indent: String) =
    when (entity) {
      is Type -> unparseType(entity, indent)
      is Variable -> unparseVariable(entity, indent)
      is Function -> unparseFunction(entity, indent)
    }

  private fun unparseType(type: Type, indent: String): String {
    val rawSource = StringBuilder()
    rawSource.appendLine("${indent}+type(\"${type.name}\") {")
    if (type.supertypes.isNotEmpty()) {
      rawSource.appendLine("$indent  ${unparseSupertypes(type.supertypes)}")
    }
    if (type.modifiers.isNotEmpty()) {
      rawSource.appendLine("$indent  ${unparseModifiers(type.modifiers)}")
    }
    if (type.members.isNotEmpty()) {
      rawSource.append(unparseChildren(type.members, "$indent  "))
    }
    rawSource.append("${indent}}")
    return rawSource.toString()
  }

  private fun unparseVariable(variable: Variable, indent: String): String {
    val rawSource = StringBuilder()
    rawSource.appendLine("${indent}+variable(\"${variable.name}\") {")
    if (variable.modifiers.isNotEmpty()) {
      rawSource.appendLine("$indent  ${unparseModifiers(variable.modifiers)}")
    }
    if (variable.initializer.isNotEmpty()) {
      rawSource.appendLine("$indent  ${unparseInitializer(variable.initializer)}")
    }
    rawSource.append("${indent}}")
    return rawSource.toString()
  }

  private fun unparseFunction(function: Function, indent: String): String {
    val rawSource = StringBuilder()
    rawSource.appendLine("${indent}+function(\"${function.signature}\") {")
    if (function.parameters.isNotEmpty()) {
      rawSource.appendLine("$indent  ${unparseParameters(function.parameters)}")
    }
    if (function.modifiers.isNotEmpty()) {
      rawSource.appendLine("$indent  ${unparseModifiers(function.modifiers)}")
    }
    if (function.body.isNotEmpty()) {
      rawSource.appendLine("$indent  ${unparseBody(function.body)}")
    }
    rawSource.append("${indent}}")
    return rawSource.toString()
  }

  private fun unparseChildren(children: Collection<SourceEntity>, indent: String): String {
    val rawSource = StringBuilder()
    for (child in children) {
      rawSource.appendLine(unparseEntity(child, indent))
    }
    return rawSource.toString()
  }

  private fun unparseSupertypes(values: Collection<Identifier>) =
    unparseIdentifiers("supertypes", values)

  private fun unparseParameters(values: Collection<Identifier>) =
    unparseIdentifiers("parameters", values)

  private fun unparseModifiers(values: Collection<String>) = unparseStrings("modifiers", values)

  private fun unparseInitializer(values: Collection<String>) = unparseStrings("initializer", values)

  private fun unparseBody(values: Collection<String>) = unparseStrings("body", values)

  private fun unparseIdentifiers(field: String, values: Collection<Identifier>) =
    unparseStrings(field, values.map(Identifier::toString))

  private fun unparseStrings(field: String, values: Collection<String>) =
    values.joinToString(prefix = "$field(", postfix = ")") { "\"$it\"" }
}
