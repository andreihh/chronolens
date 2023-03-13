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

import org.chronolens.model.Function
import org.chronolens.model.Identifier
import org.chronolens.model.Signature
import org.chronolens.model.SourceEntity
import org.chronolens.model.SourceFile
import org.chronolens.model.SourcePath
import org.chronolens.model.Type
import org.chronolens.model.Variable
import org.chronolens.model.returnTypeModifierOf
import org.chronolens.model.typeModifierOf
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.EnumConstantDeclaration
import org.eclipse.jdt.core.dom.Initializer
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.VariableDeclaration

internal data class ParserContext(private val path: SourcePath, private val source: String) {
  private fun ASTNode.toSource(): String = source.substring(startPosition, startPosition + length)

  private fun ASTNode.modifierSet(): Set<String> {
    val additionalModifier =
      when (this) {
        is AbstractTypeDeclaration -> typeModifier()
        is MethodDeclaration -> returnType()?.let(::returnTypeModifierOf)
        else -> type()?.let(::typeModifierOf)
      }
    val modifiers = getModifiers(this).map { it.toSource() }.requireDistinct()
    return modifiers + listOfNotNull(additionalModifier)
  }

  private fun MethodDeclaration.body(): List<String> = body?.toSource().toBlock()

  private fun VariableDeclaration.initializer(): List<String> = initializer?.toSource().toBlock()

  private fun EnumConstantDeclaration.initializer(): List<String> =
    anonymousClassDeclaration?.toSource().toBlock()

  private fun AnnotationTypeMemberDeclaration.defaultValue(): List<String> =
    default?.toSource().toBlock()

  private fun visitMember(node: Any?): SourceEntity? =
    when (node) {
      is AbstractTypeDeclaration -> visit(node)
      is AnnotationTypeMemberDeclaration -> visit(node)
      is EnumConstantDeclaration -> visit(node)
      is VariableDeclaration -> visit(node)
      is MethodDeclaration -> visit(node)
      is Initializer -> null
      else -> throw AssertionError("Unknown declaration '$node'!")
    }

  private fun visit(node: AbstractTypeDeclaration): Type {
    requireNotMalformed(node)
    requireValidIdentifier(node.name())
    node.supertypes().forEach(::requireValidIdentifier)
    val members = node.members().mapNotNull(::visitMember)
    members.map(SourceEntity::id).requireDistinct()
    return Type(
      Identifier(node.name()),
      node.supertypes().map(::Identifier).toSet(),
      node.modifierSet(),
      members.toSet(),
    )
  }

  private fun visit(node: AnnotationTypeMemberDeclaration): Variable {
    requireNotMalformed(node)
    requireValidIdentifier(node.name())
    return Variable(Identifier(node.name()), node.modifierSet(), node.defaultValue())
  }

  private fun visit(node: EnumConstantDeclaration): Variable {
    requireNotMalformed(node)
    requireValidIdentifier(node.name())
    return Variable(Identifier(node.name()), node.modifierSet(), node.initializer())
  }

  private fun visit(node: VariableDeclaration): Variable {
    requireNotMalformed(node)
    requireValidIdentifier(node.name())
    return Variable(Identifier(node.name()), node.modifierSet(), node.initializer())
  }

  private fun visit(node: MethodDeclaration): Function {
    requireNotMalformed(node)
    requireValidSignature(node.signature())
    node.parameterList().onEach(::requireValidIdentifier).requireDistinct()
    return Function(
      Signature(node.signature()),
      node.parameterList().map(::Identifier),
      node.modifierSet(),
      node.body(),
    )
  }

  fun visit(node: CompilationUnit): SourceFile {
    requireNotMalformed(node)
    val entities = node.types().requireIsInstance<AbstractTypeDeclaration>().map(::visit)
    entities.map(Type::name).requireDistinct()
    return SourceFile(path, entities.toSet())
  }
}
