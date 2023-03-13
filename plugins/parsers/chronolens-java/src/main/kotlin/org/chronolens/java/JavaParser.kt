/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.java

import org.chronolens.api.parsing.Parser
import org.chronolens.api.parsing.SyntaxErrorException
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT
import org.eclipse.jdt.core.dom.CompilationUnit

/** Java 11 language parser. */
internal class JavaParser : Parser {
  override fun canParse(path: SourcePath): Boolean = path.toString().endsWith(".java")

  @Throws(SyntaxErrorException::class)
  override fun parse(path: SourcePath, rawSource: String): SourceFile {
    if (!canParse(path)) {
      throw SyntaxErrorException("Cannot parse source path '$path'!")
    }
    val options = JavaCore.getOptions()
    JavaCore.setComplianceOptions(JavaCore.VERSION_11, options)
    val jdtParser =
      ASTParser.newParser(AST.JLS_Latest).apply {
        setKind(K_COMPILATION_UNIT)
        setCompilerOptions(options)
        setSource(rawSource.toCharArray())
      }
    val compilationUnit = jdtParser.createAST(null) as CompilationUnit
    return ParserContext(path, rawSource).visit(compilationUnit)
  }
}
