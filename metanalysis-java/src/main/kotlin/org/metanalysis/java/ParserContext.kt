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
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.eclipse.jdt.core.dom.VariableDeclaration
import org.metanalysis.core.model.Function
import org.metanalysis.core.model.SourceEntity
import org.metanalysis.core.model.SourceNode.Companion.ENTITY_SEPARATOR
import org.metanalysis.core.model.SourceUnit
import org.metanalysis.core.model.Type
import org.metanalysis.core.model.Variable
import org.metanalysis.java.JavaParser.Companion.toBlock

internal data class ParserContext(
    private val unitId: String,
    private val source: String,
    private val parentId: String
) {

    private fun ASTNode.toSource(): String =
        source.substring(startPosition, startPosition + length)

    private fun List<ASTNode>.toSupertypeSet(): Set<String> =
        map { it.toSource() }.requireDistinct()

    private fun List<ASTNode>.toModifierSet(): Set<String> =
        map { it.toSource() }.requireDistinct()

    // TODO: fix if javadoc contains `{`
    private fun MethodDeclaration.body(): List<String> =
        toSource().dropWhile { it != '{' }.toBlock()

    // TODO: fix if javadoc contains `=`
    private fun VariableDeclaration.initializer(): List<String> =
        toSource()
            .substringAfter(delimiter = '=', missingDelimiterValue = "")
            .toBlock()

    private fun EnumConstantDeclaration.initializer(): List<String> =
        toSource().toBlock()

    private fun AnnotationTypeMemberDeclaration.defaultValue(): List<String> =
        default?.toSource()?.toBlock() ?: emptyList()

    private fun getEntityId(simpleId: String): String =
        "$parentId$ENTITY_SEPARATOR$simpleId"

    private fun visit(node: AbstractTypeDeclaration): Type {
        requireNotMalformed(node)
        val id = getEntityId(node.name())
        val childContext = copy(parentId = id)
        val members = node.members().mapNotNull { member ->
            when (member) {
                is AbstractTypeDeclaration -> childContext.visit(member)
                is AnnotationTypeMemberDeclaration -> childContext.visit(member)
                is EnumConstantDeclaration -> childContext.visit(member)
                is VariableDeclaration -> childContext.visit(member)
                is MethodDeclaration -> childContext.visit(member)
                is Initializer -> null // TODO: parse initializers
                else -> throw AssertionError("Unknown declaration $member!")
            }
        }
        members.map(SourceEntity::id).requireDistinct()
        val modifiers =
            getModifiers(node).toModifierSet() + node.getTypeModifier()
        return Type(
            id = id,
            supertypes = node.supertypes().toSupertypeSet(),
            modifiers = modifiers,
            members = members
        )
    }

    private fun visit(node: AnnotationTypeMemberDeclaration): Variable {
        requireNotMalformed(node)
        return Variable(
            id = getEntityId(node.name()),
            modifiers = emptySet(),
            initializer = node.defaultValue()
        )
    }

    private fun visit(node: EnumConstantDeclaration): Variable {
        requireNotMalformed(node)
        return Variable(
            id = getEntityId(node.name()),
            modifiers = emptySet(),
            initializer = node.initializer()
        )
    }

    private fun visit(node: VariableDeclaration): Variable {
        requireNotMalformed(node)
        return Variable(
                id = getEntityId(node.name()),
                modifiers = getModifiers(node).toModifierSet(),
                initializer = node.initializer()
        )
    }

    private fun visit(node: MethodDeclaration): Function {
        requireNotMalformed(node)
        val parameterNodes =
                node.parameters().requireIsInstance<SingleVariableDeclaration>()
        val parameterTypes = parameterNodes.map {
            "${it.type}${if (it.isVarargs) "..." else ""}"
        }
        val signature = "${node.name()}(${parameterTypes.joinToString()})"
        val id = getEntityId(signature)
        val childContext = copy(parentId = id)
        val parameters = parameterNodes.map(childContext::visit)
        parameters.map(Variable::id).requireDistinct()
        return Function(
                id = id,
                parameters = parameters,
                modifiers = getModifiers(node).toModifierSet(),
                body = node.body()
        )
    }

    fun visit(node: CompilationUnit): SourceUnit {
        requireNotMalformed(node)
        val childContext = copy(parentId = unitId)
        return node.types()
                .requireIsInstance<AbstractTypeDeclaration>()
                .map(childContext::visit)
                .let { SourceUnit(unitId, it) }
    }
}
