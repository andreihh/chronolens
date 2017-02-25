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

package org.chronos.java

import org.chronos.core.Node
import org.chronos.core.Parser
import org.chronos.core.SourceFile
import org.chronos.core.SyntaxError
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.EnumConstantDeclaration
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.Initializer
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.eclipse.jdt.core.dom.VariableDeclaration

class JavaParser : Parser() {
    private class JavaVisitor {
        fun visit(node: AbstractTypeDeclaration): Node.Type {
            val members = node.bodyDeclarations().flatMap { member ->
                when (member) {
                    is AbstractTypeDeclaration -> listOf(visit(member))
                    is AnnotationTypeMemberDeclaration -> TODO()
                    is EnumConstantDeclaration -> TODO()
                    is FieldDeclaration -> member.fragments()
                            .filterIsInstance<VariableDeclaration>()
                            .map { visit(it) }
                    is Initializer -> TODO()
                    is MethodDeclaration -> listOf(visit(member))
                    else -> throw SyntaxError("Unknown declaration $member!")
                }
            }
            return Node.Type("${node.name}", "Type(${node.name})", members)
        }

        fun visit(node: VariableDeclaration): Node.Variable = Node.Variable(
                name = "${node.name}",
                signature = "Variable(${node.name})",
                initializer = node.initializer?.toString()
        )

        fun visit(node: MethodDeclaration): Node.Function {
            val name = "${node.name}"
            val parameters = node.parameters()
                    .filterIsInstance<SingleVariableDeclaration>()
            val parameterTypes = parameters.map { it.type }
            val signature = "Function($name(${parameterTypes.joinToString()}))"
            return Node.Function(
                    name = name,
                    signature = signature,
                    parameters = parameters.map { visit(it) },
                    body = node.body?.toString()
            )
        }

        fun visit(node: CompilationUnit): SourceFile =
                node.types().filterIsInstance<AbstractTypeDeclaration>()
                        .map { visit(it) }.let { SourceFile(it) }
    }

    @Throws(SyntaxError::class)
    override fun parse(source: String): SourceFile {
        try {
            val jdtParser = ASTParser.newParser(AST.JLS8)
            jdtParser.setSource(source.toCharArray())
            val options = JavaCore.getOptions()
            JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options)
            jdtParser.setCompilerOptions(options)
            val cu = jdtParser.createAST(null) as CompilationUnit
            return JavaVisitor().visit(cu)
        } catch (e: SyntaxError) {
            throw e
        } catch (e: Exception) {
            throw SyntaxError(cause = e)
        }
    }
}
