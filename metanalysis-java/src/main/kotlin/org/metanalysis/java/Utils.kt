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
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration
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

internal fun <T> Collection<T>.requireDistinct(): Set<T> {
    val unique = toSet()
    if (size != unique.size) {
        throw SyntaxErrorException()
    }
    return unique
}

internal inline fun <reified T> List<*>.requireIsInstance(): List<T> {
    if (any { it !is T }) {
        throw SyntaxErrorException()
    }
    @Suppress("UNCHECKED_CAST")
    return this as List<T>
}

internal fun AbstractTypeDeclaration.supertypes(): List<Type> = when (this) {
    is AnnotationTypeDeclaration -> emptyList()
    is EnumDeclaration -> superInterfaceTypes()
    is TypeDeclaration -> superInterfaceTypes() + superclassType
    else -> throw AssertionError("Unknown declaration '$this'!")
}.requireIsInstance<Type?>().filterNotNull()

private fun List<*>.toModifiers(): List<ASTNode> =
        requireIsInstance<IExtendedModifier>().requireIsInstance()

internal fun AbstractTypeDeclaration.getTypeModifier(): String = when (this) {
    is AnnotationTypeDeclaration -> "@interface"
    is EnumDeclaration -> "enum"
    is TypeDeclaration -> if (isInterface) "interface" else "class"
    else -> throw AssertionError("Unknown declaration '$this'!")
}

internal fun getModifiers(variable: VariableDeclaration): List<ASTNode> =
        when (variable) {
            is SingleVariableDeclaration -> variable.modifiers().toModifiers()
            is VariableDeclarationFragment ->
                (variable.parent as? FieldDeclaration)
                        ?.modifiers()?.toModifiers() ?: emptyList()
            else -> throw AssertionError("Unknown variable '$variable'!")
        }

internal fun getModifiers(type: AbstractTypeDeclaration): List<ASTNode> =
        type.modifiers().toModifiers()

internal fun getModifiers(method: MethodDeclaration): List<ASTNode> =
        method.modifiers().toModifiers()

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
internal fun AbstractTypeDeclaration.members(): List<*> {
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

/** Returns the name of this node. */
internal fun AbstractTypeDeclaration.name(): String = name.identifier
internal fun AnnotationTypeMemberDeclaration.name(): String = name.identifier
internal fun EnumConstantDeclaration.name(): String = name.identifier
internal fun MethodDeclaration.name(): String = name.identifier
internal fun VariableDeclaration.name(): String = name.identifier

internal fun requireNotMalformed(node: ASTNode) {
    if ((node.flags and (ASTNode.MALFORMED or ASTNode.RECOVERED)) != 0) {
        throw SyntaxErrorException()
    }
}
