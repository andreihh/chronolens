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
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.EnumConstantDeclaration
import org.eclipse.jdt.core.dom.EnumDeclaration
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.IExtendedModifier
import org.eclipse.jdt.core.dom.Initializer
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.eclipse.jdt.core.dom.Type
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.dom.VariableDeclaration
import org.eclipse.jdt.core.dom.VariableDeclarationFragment

import org.metanalysis.core.model.Node
import org.metanalysis.core.model.Parser
import org.metanalysis.core.model.SourceFile

/** Java 8 language parser. */
class JavaParser : Parser() {
    companion object {
        /** The `Java` programming language supported by this parser. */
        const val LANGUAGE: String = "Java"

        /** The Java file extensions. */
        val EXTENSIONS: Set<String> = setOf("java")

        /**
         * Formats this string to a block of code by splitting it into lines
         * (delimited by `\n`), removing blank lines (those which consist only
         * of whitespaces) and trims leading and trailing whitespaces from all
         * lines.
         */
        @JvmStatic fun String.toBlock(): List<String> =
                lines().filter(String::isNotBlank).map(String::trim)

        @Throws(SyntaxError::class)
        private fun <T> Collection<T>.requireDistinct(): Set<T> {
            val unique = toSet()
            if (size != unique.size) {
                throw SyntaxError("$this contains duplicated elements!")
            }
            return unique
        }

        @Throws(SyntaxError::class)
        private inline fun <reified T> List<*>.requireIsInstance(): List<T> =
                map {
                    if (it is T) it
                    else throw SyntaxError("$this contains invalid type $it!")
                }
    }

    private data class Context(private val source: String) {
        fun ASTNode.toSource() =
                source.substring(startPosition, startPosition + length)

        fun getBody(method: MethodDeclaration) =
                method.toSource().dropWhile { it != '{' }.toBlock()

        fun getInitializer(variable: VariableDeclaration) =
                variable.toSource().substringAfter('=', "").toBlock()

        fun getInitializer(enumConstant: EnumConstantDeclaration) =
                enumConstant.toSource().toBlock()

        fun getDefaultValue(annotationMember: AnnotationTypeMemberDeclaration) =
                annotationMember.default?.toSource()?.toBlock() ?: emptyList()

        fun AbstractTypeDeclaration.supertypes() = when (this) {
            is AnnotationTypeDeclaration -> emptyList()
            is EnumDeclaration -> superInterfaceTypes()
            is TypeDeclaration -> superInterfaceTypes() + superclassType
            else -> throw AssertionError("Unknown declaration $this!")
        }.requireIsInstance<Type?>().mapNotNull { it?.toSource() }

        private fun List<*>.toModifierSet() =
                requireIsInstance<IExtendedModifier>()
                        .requireIsInstance<ASTNode>()
                        .map { it.toSource() }
                        .requireDistinct()

        fun getModifiers(variable: VariableDeclaration) = when (variable) {
            is SingleVariableDeclaration -> variable.modifiers().toModifierSet()
            is VariableDeclarationFragment ->
                (variable.parent as? FieldDeclaration)
                        ?.modifiers()?.toModifierSet()
                        ?: emptySet<String>()
            else -> throw AssertionError("Unknown variable $variable!")
        }

        fun getModifiers(type: AbstractTypeDeclaration) =
                type.modifiers().toModifierSet()

        fun getModifiers(method: MethodDeclaration) =
                method.modifiers().toModifierSet()
    }

    /** Returns the name of this node. */
    private fun AbstractTypeDeclaration.name() = name.identifier
    private fun AnnotationTypeMemberDeclaration.name() = name.identifier
    private fun EnumConstantDeclaration.name() = name.identifier
    private fun MethodDeclaration.name() = name.identifier
    private fun VariableDeclaration.name(): String = name.identifier

    /**
     * Returns the list of all members of this type.
     *
     * The returned elements can be of the following types:
     * - [AbstractTypeDeclaration]
     * - [AnnotationTypeMemberDeclaration]
     * - [EnumConstantDeclaration]
     * - [VariableDeclaration]
     * - [MethodDeclaration]
     * - [Initializer]
     */
    private fun AbstractTypeDeclaration.members(): List<*> {
        val declarations = arrayListOf<Any?>()
        if (this is EnumDeclaration) {
            declarations.addAll(enumConstants())
        }
        bodyDeclarations().flatMapTo(declarations) { member ->
            if (member is FieldDeclaration) member.fragments()
            else listOf(member)
        }
        return declarations
    }

    private fun Context.visit(node: AbstractTypeDeclaration): Node.Type {
        val members = node.members().map { member ->
            when (member) {
                is AbstractTypeDeclaration -> visit(member)
                is AnnotationTypeMemberDeclaration -> visit(member)
                is EnumConstantDeclaration -> visit(member)
                is VariableDeclaration -> visit(member)
                is MethodDeclaration -> visit(member)
                is Initializer -> throw TODO("Can't parse initializers!")
                else -> throw AssertionError("Unknown declaration $member!")
            }
        }
        return Node.Type(
                name = node.name(),
                supertypes = node.supertypes().requireDistinct(),
                members = members.requireDistinct(),
                modifiers = getModifiers(node)
        )
    }

    private fun Context.visit(node: AnnotationTypeMemberDeclaration) =
            Node.Variable(node.name(), getDefaultValue(node))

    private fun Context.visit(node: EnumConstantDeclaration) =
            Node.Variable(node.name(), getInitializer(node))

    private fun Context.visit(node: VariableDeclaration) =
            Node.Variable(node.name(), getInitializer(node), getModifiers(node))

    private fun Context.visit(node: MethodDeclaration): Node.Function {
        val parameters =
                node.parameters().requireIsInstance<SingleVariableDeclaration>()
        val parameterTypes = parameters.map {
            "${it.type}${if (it.isVarargs) "..." else ""}"
        }
        return Node.Function(
                signature = "${node.name()}(${parameterTypes.joinToString()})",
                parameters = parameters.map { visit(it) },
                body = getBody(node),
                modifiers = getModifiers(node)
        )
    }

    private fun Context.visit(node: CompilationUnit): SourceFile =
            node.types().requireIsInstance<AbstractTypeDeclaration>()
                    .map { visit(it) }.requireDistinct().let(::SourceFile)

    override val language: String = LANGUAGE
    override val extensions: Set<String> = EXTENSIONS

    @Throws(SyntaxError::class)
    override fun parse(source: String): SourceFile = try {
        val options = JavaCore.getOptions()
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options)
        val jdtParser = ASTParser.newParser(AST.JLS8).apply {
            setKind(K_COMPILATION_UNIT)
            setCompilerOptions(options)
            setIgnoreMethodBodies(true)
            setSource(source.toCharArray())
        }
        val compilationUnit = jdtParser.createAST(null) as CompilationUnit
        Context(source).visit(compilationUnit)
    } catch (e: NotImplementedError) {
        throw SyntaxError(e)
    }
}
