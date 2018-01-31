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

package org.metanalysis.java

import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT
import org.eclipse.jdt.core.dom.CompilationUnit
import org.metanalysis.core.model.SourceUnit
import org.metanalysis.core.parsing.Parser

/** Java 8 language parser. */
class JavaParser : Parser() {
    override val language: String get() = LANGUAGE

    override fun canParse(path: String): Boolean = path.endsWith(".java")

    override fun parse(path: String, rawSource: String): SourceUnit {
        val options = JavaCore.getOptions()
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options)
        val jdtParser = ASTParser.newParser(AST.JLS9).apply {
            setKind(K_COMPILATION_UNIT)
            setCompilerOptions(options)
            setIgnoreMethodBodies(true)
            setSource(rawSource.toCharArray())
        }
        val compilationUnit = jdtParser.createAST(null) as CompilationUnit
        return ParserContext(unitId = path, source = rawSource, parentId = "")
            .visit(compilationUnit)
    }

    companion object {
        /** The `Java` programming language supported by this parser. */
        const val LANGUAGE: String = "Java"

        /**
         * Formats this string to a block of code by splitting it into lines
         * (delimited by `\n`), removing blank lines (those which consist only
         * of whitespaces) and trims leading and trailing whitespaces from all
         * lines.
         */
        @JvmStatic
        fun String.toBlock(): List<String> =
            lines().filter(String::isNotBlank).map(String::trim)
    }
}
