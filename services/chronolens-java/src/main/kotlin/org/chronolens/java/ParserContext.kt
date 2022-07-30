/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.Function
import org.chronolens.core.model.QualifiedId.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.QualifiedId.Companion.MEMBER_SEPARATOR
import org.chronolens.core.model.SourceEntity
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import org.chronolens.core.model.returnTypeModifierOf
import org.chronolens.core.model.typeModifierOf
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.EnumConstantDeclaration
import org.eclipse.jdt.core.dom.Initializer
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.VariableDeclaration

internal data class ParserContext(
    private val path: String,
    private val source: String,
    private val parentId: String
) {

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

    private fun getTypeId(name: String): String = "$parentId$CONTAINER_SEPARATOR$name"

    private fun getFunctionId(signature: String): String = "$parentId$MEMBER_SEPARATOR$signature"

    private fun getVariableId(name: String): String = "$parentId$MEMBER_SEPARATOR$name"

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
        val id = getTypeId(node.name())
        val childContext = copy(parentId = id)
        val members = node.members().mapNotNull(childContext::visitMember)
        members.map(SourceEntity::simpleId).requireDistinct()
        return Type(
            name = node.name(),
            supertypes = node.supertypes(),
            modifiers = node.modifierSet(),
            members = members.toSet()
        )
    }

    private fun visit(node: AnnotationTypeMemberDeclaration): Variable {
        requireNotMalformed(node)
        return Variable(node.name(), node.modifierSet(), node.defaultValue())
    }

    private fun visit(node: EnumConstantDeclaration): Variable {
        requireNotMalformed(node)
        return Variable(node.name(), node.modifierSet(), node.initializer())
    }

    private fun visit(node: VariableDeclaration): Variable {
        requireNotMalformed(node)
        return Variable(node.name(), node.modifierSet(), node.initializer())
    }

    private fun visit(node: MethodDeclaration): Function {
        requireNotMalformed(node)
        val parameters = node.parameterList()
        parameters.requireDistinct()
        return Function(
            signature = node.signature(),
            parameters = parameters,
            modifiers = node.modifierSet(),
            body = node.body()
        )
    }

    fun visit(node: CompilationUnit): SourceFile {
        requireNotMalformed(node)
        val childContext = copy(parentId = path)
        val entities =
            node.types().requireIsInstance<AbstractTypeDeclaration>().map(childContext::visit)
        entities.map(Type::name).requireDistinct()
        return SourceFile(path, entities.toSet())
    }
}
