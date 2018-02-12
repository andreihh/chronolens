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

import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.EnumConstantDeclaration
import org.eclipse.jdt.core.dom.Initializer
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.VariableDeclaration
import org.metanalysis.core.model.Function
import org.metanalysis.core.model.SourceEntity
import org.metanalysis.core.model.SourceNode.Companion.ENTITY_SEPARATOR
import org.metanalysis.core.model.SourceUnit
import org.metanalysis.core.model.Type
import org.metanalysis.core.model.Variable

internal data class ParserContext(
    private val unitId: String,
    private val source: String,
    private val parentId: String
) {

    private fun ASTNode.toSource(): String =
        source.substring(startPosition, startPosition + length)

    private fun ASTNode.modifierSet(): Set<String> =
        getModifiers(this).map { it.toSource() }.requireDistinct()

    private fun AbstractTypeDeclaration.supertypeSet(): Set<String> =
        supertypes().map { it.toSource() }.requireDistinct()

    private fun MethodDeclaration.body(): List<String> =
        body?.toSource().toBlock()

    private fun VariableDeclaration.initializer(): List<String> =
        initializer?.toSource().toBlock()

    private fun EnumConstantDeclaration.initializer(): List<String> =
        anonymousClassDeclaration?.toSource().toBlock()

    private fun AnnotationTypeMemberDeclaration.defaultValue(): List<String> =
        default?.toSource().toBlock()

    private fun getEntityId(simpleId: String): String =
        "$parentId$ENTITY_SEPARATOR$simpleId"

    private fun visitMember(node: Any?): SourceEntity? = when (node) {
        is AbstractTypeDeclaration -> visit(node)
        is AnnotationTypeMemberDeclaration -> visit(node)
        is EnumConstantDeclaration -> visit(node)
        is VariableDeclaration -> visit(node)
        is MethodDeclaration -> visit(node)
        is Initializer -> null // TODO: parse initializers
        else -> throw AssertionError("Unknown declaration $node!")
    }

    private fun visit(node: AbstractTypeDeclaration): Type {
        requireNotMalformed(node)
        val id = getEntityId(node.name())
        val childContext = copy(parentId = id)
        val members = node.members().mapNotNull(childContext::visitMember)
        members.map(SourceEntity::id).requireDistinct()
        return Type(
            id = id,
            supertypes = node.supertypeSet(),
            modifiers = node.modifierSet() + node.typeModifier(),
            members = members.toSet()
        )
    }

    private fun visit(node: AnnotationTypeMemberDeclaration): Variable {
        requireNotMalformed(node)
        return Variable(
            id = getEntityId(node.name()),
            modifiers = node.modifierSet(),
            initializer = node.defaultValue()
        )
    }

    private fun visit(node: EnumConstantDeclaration): Variable {
        requireNotMalformed(node)
        return Variable(
            id = getEntityId(node.name()),
            modifiers = node.modifierSet(),
            initializer = node.initializer()
        )
    }

    private fun visit(node: VariableDeclaration): Variable {
        requireNotMalformed(node)
        return Variable(
            id = getEntityId(node.name()),
            modifiers = node.modifierSet(),
            initializer = node.initializer()
        )
    }

    private fun visit(node: MethodDeclaration): Function {
        requireNotMalformed(node)
        val id = getEntityId(node.signature())
        val childContext = copy(parentId = id)
        val parameters = getParameters(node).map(childContext::visit)
        parameters.map(Variable::id).requireDistinct()
        return Function(
            id = id,
            parameters = parameters,
            modifiers = node.modifierSet(),
            body = node.body()
        )
    }

    fun visit(node: CompilationUnit): SourceUnit {
        requireNotMalformed(node)
        val childContext = copy(parentId = unitId)
        val entities = node.types()
            .requireIsInstance<AbstractTypeDeclaration>()
            .map(childContext::visit)
        entities.map(Type::id).requireDistinct()
        return SourceUnit(unitId, entities.toSet())
    }
}
